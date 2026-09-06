# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Free Fridges — a Kotlin Multiplatform + Compose Multiplatform app (Android + iOS) for finding and sharing
community fridges. This is a freshly scaffolded project; features are not yet implemented beyond the
starter screen.

## Commands

`JAVA_HOME` must point at a JDK 17+ (this machine has no system JRE — use Android Studio's bundled JBR):
```
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

- Build Android debug APK: `./gradlew :androidApp:assembleDebug`
- Run all Gradle checks: `./gradlew build`
- Compile the iOS framework only (no Xcode): `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
- Build the iOS app via CLI: `cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build`
- Open `iosApp/iosApp.xcodeproj` in Xcode to run/debug on a simulator or device — the "Compile Kotlin Framework"
  run-script build phase invokes `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` automatically and
  falls back to `java_home`/Android Studio's JBR if `JAVA_HOME` isn't inherited by the Xcode build environment.

There is no test suite yet.

### Running on a simulator/emulator

Neither an iOS simulator device nor an Android AVD existed on this machine originally; both are now created
(`iPhone 17 - FreeFridges` on the iOS 26.5 runtime, and the `FreeFridges` AVD on
`system-images/android-36.1/google_apis_playstore/arm64-v8a`).

**Build with Xcode 26.6** (`/Applications/Xcode-26.6.0.app`, currently selected). The `27.0 Beta` install
alongside it is **incomplete** — its `Contents/Developer/Applications/` directory is missing, so it has no
`Simulator.app`; `xcodes select "27.0 Beta"` still builds fine but leaves you with no simulator window and
`open -a Simulator` failing. Re-download it via `xcodes` before selecting it.

```
# iOS
xcrun simctl boot "iPhone 17 - FreeFridges"
open -a "$(xcode-select -p)/Applications/Simulator.app"   # otherwise it runs headless
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -destination 'id=<device-udid>' build
xcrun simctl install booted <DerivedData>/Build/Products/Debug-iphonesimulator/iosApp.app
xcrun simctl launch --console-pty booted org.freefridges.app   # --console-pty surfaces Kotlin crashes

# Android
$ANDROID_HOME/emulator/emulator -avd FreeFridges -no-snapshot-load -gpu swiftshader_indirect
$ANDROID_HOME/platform-tools/adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n org.freefridges.app/.MainActivity
```

The Android SDK here has **no `cmdline-tools`, so no `avdmanager`** — the AVD was created by hand-writing
`~/.android/avd/FreeFridges.ini` and `FreeFridges.avd/config.ini`. `assembleRelease` produces an *unsigned*
APK (no `signingConfig`); to run it, sign with the debug keystore via `zipalign` + `apksigner`.

## Architecture

Three modules: a pure KMP library (`composeApp`), a thin Android application shell (`androidApp`), and an
iOS Xcode wrapper (`iosApp`) — split apart because AGP 9 no longer allows `com.android.application` and
`org.jetbrains.kotlin.multiplatform` in the same module (see version pins below).

- `composeApp/src/commonMain` — shared Compose UI and logic. `App.kt` holds the root
  `@Composable App(isDebugBuild: Boolean)` entry point; all screens/business logic should live here unless
  they need a platform API. This module has **no Android application id, manifest components, or launcher
  resources** — those live in `androidApp`.
- `composeApp/src/androidMain` — Android-target-specific KMP code (currently just a stub manifest and the
  MapLibre render-backend dependency). Add `expect`/`actual` implementations here, not `androidApp`.
- `composeApp/src/iosMain` — `MainViewController(isDebugBuild:)` wraps `App()` via `ComposeUIViewController`,
  exported to Swift as `MainViewControllerKt.MainViewController(isDebugBuild:)`.
- `composeApp` targets `androidTarget` (via the `com.android.kotlin.multiplatform.library` plugin),
  `iosArm64`, and `iosSimulatorArm64`. **No `iosX64`** — Compose Multiplatform 1.12.0 dropped the Intel
  simulator target entirely, and the `iosApp` Xcode project excludes `x86_64` for the simulator SDK
  (`EXCLUDED_ARCHS[sdk=iphonesimulator*]`) to match.
- `androidApp/src/main` — plain Android application module (ordinary `src/main` layout, not KMP's
  `src/androidMain`). `MainActivity` calls `setContent { App(isDebugBuild = BuildConfig.DEBUG) }`; owns the
  manifest, application id, launcher icon, and theme. Depends on `composeApp` via
  `implementation(project(":composeApp"))`.
- `iosApp/iosApp` — SwiftUI shell (`iOSApp.swift`, `ContentView.swift`) that hosts the Compose UI in a
  `UIViewControllerRepresentable`. Swift code should stay minimal; app logic belongs in `commonMain`/`iosMain`.
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog); reference them via
  `libs.*` / `libs.plugins.*` in build files rather than hardcoding versions.

### App shell: tabs, navigation, and debug gating

- `App(isDebugBuild)` is the whole shell: `FreeFridgesTheme` + a Material 3 `Scaffold` + `AppBottomBar` +
  `NavHost`. Tabs are declared once in `commonMain/.../navigation/TopLevelDestination.kt` (route, label,
  icon, `debugOnly`) — add a tab there rather than editing `App.kt` or either bar implementation.
- **The bottom bar is intentionally `expect`/`actual`, not shared** (`commonMain/ui/AppBottomBar.kt` +
  `androidMain`/`iosMain` actuals). Android uses the standard Material3 `NavigationBar` (80dp, selection
  pill). iOS uses a hand-rolled bar at UIKit's 49dp content height with the icon/label pair centered and
  the selected tab tinted rather than pill-wrapped — Material's bar renders ~114pt on iPhone once the
  home-indicator inset is added, against ~83pt for a native tab bar. Keep the tab *set* common; only the
  presentation differs.
- Routes are type-safe `@Serializable` objects in `navigation/Destinations.kt`, which is why `composeApp`
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
- **Debug gating**: `isDebugBuild` is passed in by each platform shell (`BuildConfig.DEBUG` on Android —
  hence `buildFeatures { buildConfig = true }` in `androidApp`; `#if DEBUG` in `ContentView.swift` on iOS).
  `App.kt` uses it twice: to filter the tab list *and* to skip registering `composable<DebugRoute>`, so a
  release build has no route to the debug screen at all. Hang new debug-only surfaces off `DebugRoute`
  rather than inventing another gating mechanism — `ui/DebugScreen.kt` is the settings surface, currently
  just the light/dark override.
- Tab icons are Compose Multiplatform resources (Android vector XML) in
  `composeApp/src/commonMain/composeResources/drawable/`, referenced via the generated `Res.drawable.*`.
  Don't reach for `androidx.compose.material.icons` — `material-icons-core` is only on the Android side of
  the classpath here, so it won't resolve in `commonMain`.

### Version pins (read before upgrading)

- **AGP 9.4.0**, using the `com.android.kotlin.multiplatform.library` plugin for `composeApp` (not
  `com.android.library` — AGP 9 rejects that combined with the KMP plugin) and plain `com.android.application`
  for `androidApp`. AGP 9's built-in Kotlin support means `androidApp` does **not** apply
  `org.jetbrains.kotlin.android` — adding it back will fail the build ("no longer required since AGP 9.0").
- **Gradle 9.7.1** (required by AGP 9.x).
- **Compose Multiplatform 1.12.0**, **Kotlin 2.4.10**, **compileSdk/targetSdk 37**, **minSdk 26**.
- The `com.android.kotlin.multiplatform.library` plugin configures the Android target *inside* the `kotlin {}`
  block (`kotlin { android { namespace = ...; compileSdk = ...; ... } }`), not via a top-level `android {}`
  block like classic `com.android.library`/`com.android.application` — don't move that config out.
- **`kotlin { android { androidResources { enable = true } } }` in `composeApp` is load-bearing.** That
  plugin leaves Android resources off by default, which makes `variant.sources.assets` null; the Compose
  resources plugin then *silently* skips packaging `composeResources/` into the AAR (no warning, no error —
  `Res.drawable.*` compiles fine and the images just fail to load at runtime on Android only). Verify with
  `unzip -l androidApp/build/outputs/apk/debug/androidApp-debug.apk | grep composeResources`.
- **navigation-compose 2.10.0-alpha02** and **lifecycle 2.11.0** — the pairing CMP 1.12.0's release notes
  name as tested. Stable navigation 2.9.2 exists but was built against lifecycle 2.9.6.
- **maplibre-compose 0.15.0** (`org.maplibre.compose`). The group moved: anything documenting
  `dev.sargunv.maplibre-compose` (≤0.10.x) is stale, as is any pre-0.15 advice to add a `MapLibre`
  CocoaPods/SPM dependency — 0.15.0 ships the native code inside the klib and a pod will conflict with it.
  0.15.0 is built against exactly Kotlin 2.4.10 / CMP 1.12.0 / compileSdk 37, so bumping any of those
  probably means bumping this too.
  - Android needs a render-backend artifact at runtime or the map draws a blank canvas;
    `composeApp/androidMain` uses `maplibre-compose-runtime-opengl-android` (a `-vulkan-` variant exists).
  - iOS needs the system-library linker flags in `OTHER_LDFLAGS` (`-l"c++"`, `-lz`, CoreFoundation,
    CoreGraphics, CoreText, Foundation, ImageIO, Metal, QuartzCore) because the framework is static.
    Omitting them still lets `linkDebugFrameworkIosSimulatorArm64` pass and fails only at Xcode link time
    with undefined C++/zlib/Metal symbols.
  - The map style is OpenFreeMap Liberty in light mode and OpenFreeMap Dark in dark mode (free, no API
    key), selected in `MapScreen` from `LocalIsDarkTheme`. Both styles pull the same `/planet` TileJSON,
    which is where the attribution string actually comes from — so the obligation is met identically by
    the default `MapOverlay`, and replacing the overlay means rendering attribution yourself.
  - `MaplibreMap` places its overlay controls from `contentWindowInsets` **in its own measure policy**, not
    through the modifier chain, so neither `Modifier.padding` nor `consumeWindowInsets` on the map has any
    effect on them. A caller that has already inset the map away from a system bar (as `App.kt` does for
    the bottom bar) must drop that side from `contentWindowInsets` or the inset is applied twice and the
    attribution/logo/scale bar float above the map's edge.
- **iOS deployment target 15.3** (raised from 15.0 to match the only configuration MapLibre tests).
- `iosApp/Info.plist` must keep `CADisableMinimumFrameDurationOnPhone = true`. Compose UI hard-asserts on it
  at startup, so without it the app throws an uncaught Kotlin exception and dies before drawing a frame —
  and `xcrun simctl launch` without `--console-pty` shows nothing but a return to the home screen.
- `androidx.core` is a direct `composeApp/androidMain` dependency purely for
  `WindowInsetsControllerCompat`; it was already arriving transitively via compose-ui, but at whatever
  version that pulled.
- `MainActivity` passes `SystemBarStyle.auto(TRANSPARENT, TRANSPARENT)` for **both** bars. The app paints
  under both (map under the status bar, `NavigationBar` under the navigation bar), so any scrim would show
  through; `auto` takes the icon tint from the night-mode configuration, which is the same signal
  `FreeFridgesTheme` follows — it is only the *initial* value, since `ApplySystemUiTheme` re-asserts the
  tint whenever `ThemeMode` overrides the system. Leaving `navigationBarStyle` at its default is the bug
  this replaced — its scrim and dark-mode icon tint are wrong against an app-drawn bar. `androidApp/src/main/res/values-night/`
  carries the dark window-background theme so there's no white flash before the first frame.
- `ContentView.swift` uses **`.ignoresSafeArea()`**, not `.ignoresSafeArea(.keyboard)`. Insetting the
  Compose view in SwiftUI stops the Compose canvas above the home indicator, so the navigation bar's
  background stops there too and the white window background shows through as a ~100px gap under the tab
  bar. Compose applies the safe area itself via Scaffold/NavigationBar window insets — let it.
- `org.gradle.jvmargs`/`kotlin.daemon.jvmargs` are at **6 GB**: linking the release iOS framework against
  MapLibre Native OOMs at the previous 2 GB. The flip side is that a full `./gradlew build` can claim ~12 GB
  across the two daemons, which is enough for macOS to kill a running Android emulator out from under you —
  shut the emulator down before a clean build, or expect to restart it after.
