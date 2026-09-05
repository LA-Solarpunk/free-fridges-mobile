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

## Architecture

Three modules: a pure KMP library (`composeApp`), a thin Android application shell (`androidApp`), and an
iOS Xcode wrapper (`iosApp`) — split apart because AGP 9 no longer allows `com.android.application` and
`org.jetbrains.kotlin.multiplatform` in the same module (see version pins below).

- `composeApp/src/commonMain` — shared Compose UI and logic. `App.kt` holds the root `@Composable App()`
  entry point; all screens/business logic should live here unless they need a platform API. This module has
  **no Android application id, manifest components, or launcher resources** — those live in `androidApp`.
- `composeApp/src/androidMain` — Android-target-specific KMP code (currently empty beyond a stub manifest).
  Add `expect`/`actual` implementations here, not `androidApp`.
- `composeApp/src/iosMain` — `MainViewController()` wraps `App()` via `ComposeUIViewController`, exported to
  Swift as `MainViewControllerKt.MainViewController()`.
- `composeApp` targets `androidTarget` (via the `com.android.kotlin.multiplatform.library` plugin),
  `iosArm64`, and `iosSimulatorArm64`. **No `iosX64`** — Compose Multiplatform 1.12.0 dropped the Intel
  simulator target entirely, and the `iosApp` Xcode project excludes `x86_64` for the simulator SDK
  (`EXCLUDED_ARCHS[sdk=iphonesimulator*]`) to match.
- `androidApp/src/main` — plain Android application module (ordinary `src/main` layout, not KMP's
  `src/androidMain`). `MainActivity` calls `setContent { App() }`; owns the manifest, application id,
  launcher icon, and theme. Depends on `composeApp` via `implementation(project(":composeApp"))`.
- `iosApp/iosApp` — SwiftUI shell (`iOSApp.swift`, `ContentView.swift`) that hosts the Compose UI in a
  `UIViewControllerRepresentable`. Swift code should stay minimal; app logic belongs in `commonMain`/`iosMain`.
- Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog); reference them via
  `libs.*` / `libs.plugins.*` in build files rather than hardcoding versions.

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
