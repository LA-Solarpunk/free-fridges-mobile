# composeApp

The shared KMP library: all Compose UI and logic for both platforms. See the root `CLAUDE.md` for the
module split and cross-cutting version pins.

## Source sets

- `src/commonMain` — shared Compose UI and logic. `App.kt` holds the root `@Composable App(isDebugBuild: Boolean)`
  entry point; screens and business logic live here unless they need a platform API. This module has **no
  Android application id, manifest components, or launcher resources** — those live in `androidApp`.
- `src/androidMain` — Android-target-specific KMP code (a stub manifest, the MapLibre render backend, and
  the `actual`s for `AppBottomBar`/`ApplySystemUiTheme`). Add `expect`/`actual` implementations here, not
  in `androidApp`.
- `src/iosMain` — `MainViewController(isDebugBuild:)` wraps `App()` via `ComposeUIViewController`, exported
  to Swift as `MainViewControllerKt.MainViewController(isDebugBuild:)`, plus the iOS `actual`s.

Targets are `androidTarget` (via the `com.android.kotlin.multiplatform.library` plugin), `iosArm64`, and
`iosSimulatorArm64`. **No `iosX64`** — Compose Multiplatform 1.12.0 dropped the Intel simulator target
entirely, and the `iosApp` Xcode project excludes `x86_64` for the simulator SDK to match.

## App shell: tabs, navigation, and debug gating

- `App(isDebugBuild)` is the whole shell: `FreeFridgesTheme` + a Material 3 `Scaffold` + `AppBottomBar` +
  `NavHost`. Tabs are declared once in `commonMain/.../navigation/TopLevelDestination.kt` (route, label,
  icon, `debugOnly`) — add a tab there rather than editing `App.kt` or either bar implementation.
- **The bottom bar is intentionally `expect`/`actual`, not shared** (`commonMain/ui/AppBottomBar.kt` +
  `androidMain`/`iosMain` actuals). Android uses the standard Material3 `NavigationBar` (80dp, selection
  pill). iOS uses a hand-rolled bar at UIKit's 49dp content height with the icon/label pair centered and
  the selected tab tinted rather than pill-wrapped — Material's bar renders ~114pt on iPhone once the
  home-indicator inset is added, against ~83pt for a native tab bar. Keep the tab *set* common; only the
  presentation differs.
- Routes are type-safe `@Serializable` objects in `navigation/Destinations.kt`, which is why this module
  applies `org.jetbrains.kotlin.plugin.serialization`. The `kotlinx-serialization-core` runtime arrives
  transitively via navigation-compose; only the compiler plugin is applied explicitly. Every route also
  implements the `sealed interface AppRoute` — navigation's route API takes `Any` and resolves the
  serializer at runtime, so without that marker a route that forgot `@Serializable` or its `composable<T>`
  registration compiles fine and crashes on first tap. Declare new routes in that file so they get it.
- **Theme and dark mode**: `commonMain/ui/theme/Theme.kt` holds `ThemeMode` (System/Light/Dark) and
  `FreeFridgesTheme`, which resolves the mode against `isSystemInDarkTheme()` and publishes the result as
  `LocalIsDarkTheme`. Anything that has to pick a light/dark asset for itself — currently just the map
  style — should read that composition local rather than calling `isSystemInDarkTheme()` again, so it
  follows an override too. The color schemes are the M3 baselines; a brand palette drops in there without
  touching call sites.
  - `App` owns the `ThemeMode` in a `rememberSaveable` (enums need a `Saver` — `ThemeMode.Saver`) and the
    debug screen is the only thing that can change it, so a release build is always `System`.
  - **`ApplySystemUiTheme(themeMode, darkTheme)` is `expect`/`actual`** (`ui/theme/SystemUiTheme.kt` +
    actuals), because the status/navigation bar icon tint is platform chrome that Compose can't set.
    Android re-asserts it through `WindowInsetsControllerCompat` — `enableEdgeToEdge` picks it once from
    the night-mode config, which goes stale the moment the mode overrides the system. iOS sets the
    window's `overrideUserInterfaceStyle` via `UIWindowScene.keyWindow`, which is what the status bar text
    color derives from. It takes the *mode* as well as the resolved boolean on purpose: `System` has to map
    to `Unspecified` on iOS, since pinning the current value would stop the trait tracking the OS.
- **Debug gating**: `isDebugBuild` is passed in by each platform shell (`BuildConfig.DEBUG` on Android,
  `#if DEBUG` in `ContentView.swift` on iOS). `App.kt` uses it twice: to filter the tab list *and* to skip
  registering `composable<DebugRoute>`, so a release build has no route to the debug screen at all. Hang
  new debug-only surfaces off `DebugRoute` rather than inventing another gating mechanism —
  `ui/DebugScreen.kt` is the settings surface, currently just the light/dark override.
- Tab icons are Compose Multiplatform resources (Android vector XML) in `src/commonMain/composeResources/drawable/`,
  referenced via the generated `Res.drawable.*`. Don't reach for `androidx.compose.material.icons` —
  `material-icons-core` is only on the Android side of the classpath here, so it won't resolve in
  `commonMain`.

## Build configuration

- The `com.android.kotlin.multiplatform.library` plugin configures the Android target *inside* the
  `kotlin {}` block (`kotlin { android { namespace = ...; compileSdk = ...; ... } }`), not via a top-level
  `android {}` block like classic `com.android.library`/`com.android.application` — don't move that config
  out.
- **`kotlin { android { androidResources { enable = true } } }` is load-bearing.** That plugin leaves
  Android resources off by default, which makes `variant.sources.assets` null; the Compose resources plugin
  then *silently* skips packaging `composeResources/` into the AAR (no warning, no error — `Res.drawable.*`
  compiles fine and the images just fail to load at runtime on Android only). Verify with
  `unzip -l androidApp/build/outputs/apk/debug/androidApp-debug.apk | grep composeResources`.
- `androidx.core` is a direct `androidMain` dependency purely for `WindowInsetsControllerCompat`; it was
  already arriving transitively via compose-ui, but at whatever version that pulled.

## Dependency pins (read before upgrading)

- **navigation-compose 2.10.0-alpha02** and **lifecycle 2.11.0** — the pairing CMP 1.12.0's release notes
  name as tested. Stable navigation 2.9.2 exists but was built against lifecycle 2.9.6.
- **maplibre-compose 0.15.0** (`org.maplibre.compose`). The group moved: anything documenting
  `dev.sargunv.maplibre-compose` (≤0.10.x) is stale, as is any pre-0.15 advice to add a `MapLibre`
  CocoaPods/SPM dependency — 0.15.0 ships the native code inside the klib and a pod will conflict with it.
  0.15.0 is built against exactly Kotlin 2.4.10 / CMP 1.12.0 / compileSdk 37, so bumping any of those
  probably means bumping this too.
  - Android needs a render-backend artifact at runtime or the map draws a blank canvas; `androidMain` uses
    `maplibre-compose-runtime-opengl-android` (a `-vulkan-` variant exists).
  - iOS needs system-library linker flags in the Xcode project because the framework is static — see
    `iosApp/CLAUDE.md`.
  - The map style is OpenFreeMap Liberty in light mode and OpenFreeMap Dark in dark mode (free, no API
    key), selected in `MapScreen` from `LocalIsDarkTheme`. Both styles pull the same `/planet` TileJSON,
    which is where the attribution string actually comes from — so the obligation is met identically by
    the default `MapOverlay`, and replacing the overlay means rendering attribution yourself.
  - `MaplibreMap` places its overlay controls from `contentWindowInsets` **in its own measure policy**, not
    through the modifier chain, so neither `Modifier.padding` nor `consumeWindowInsets` on the map has any
    effect on them. A caller that has already inset the map away from a system bar (as `App.kt` does for
    the bottom bar) must drop that side from `contentWindowInsets` or the inset is applied twice and the
    attribution/logo/scale bar float above the map's edge.
