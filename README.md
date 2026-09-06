# Free Fridges

[![Xcode 26.6](https://img.shields.io/badge/Xcode-26.6-147EFB?logo=xcode&logoColor=white)](https://developer.apple.com/xcode/)
[![iOS 15.3+](https://img.shields.io/badge/iOS-15.3%2B-000000?logo=apple&logoColor=white)](iosApp/)
[![Android API 26+](https://img.shields.io/badge/Android-API%2026%2B%20%7C%20target%2037-3DDC84?logo=android&logoColor=white)](androidApp/)

A Kotlin Multiplatform + Compose Multiplatform app (Android and iOS) for finding and sharing community
fridges.

**Status: early.** What exists today is the app shell — a bottom tab bar, a MapLibre map tab, a
debug-only settings tab, and placeholder screens for the rest. The fridge features themselves are not
built yet, and there is no test suite.

## Requirements

- **JDK 17+.** `JAVA_HOME` must point at one. On macOS, Android Studio's bundled JBR works:
  ```sh
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  ```
- **Android SDK** with compileSdk 37. The app targets API 37 and supports back to **API 26** (Android 8.0).
- **Xcode 26.6** for the iOS app, which deploys to **iOS 15.3+** (raised from 15.0 to match the only
  configuration MapLibre tests). Apple-silicon Macs only — the Intel simulator target was dropped in
  Compose Multiplatform 1.12.0, so there is no `iosX64` build.

Gradle itself is provided by the wrapper (`./gradlew`); it does not need to be installed.

## Building

```sh
# Android debug APK
./gradlew :androidApp:assembleDebug

# All Gradle checks
./gradlew build

# Compile the iOS framework only, without Xcode
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Build the iOS app from the command line
cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp \
  -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

For iOS you can also just open `iosApp/iosApp.xcodeproj` in Xcode and run — a build phase invokes
Gradle to compile and embed the Kotlin framework for you.

Running on a device, simulator, or emulator depends on hardware and virtual devices set up on your own
machine, so those steps aren't captured here.

> **Note:** the Gradle and Kotlin daemons are configured with 6 GB heaps each (linking the release iOS
> framework against MapLibre Native needs it). A full `./gradlew build` can therefore claim ~12 GB, which
> is enough for macOS to kill a running Android emulator — shut the emulator down before a clean build.

## Project structure

Three modules, split apart because AGP 9 no longer allows `com.android.application` and the Kotlin
Multiplatform plugin in the same module:

| Module | What's in it |
| --- | --- |
| [`composeApp`](composeApp/) | All shared Compose UI and logic, plus the `expect`/`actual` implementations that need a platform API. Screens and business logic go here. |
| [`androidApp`](androidApp/) | A thin Android application shell: manifest, application id, launcher icon, theme, `MainActivity`. |
| [`iosApp`](iosApp/) | The Xcode project and a minimal SwiftUI shell hosting the Compose UI. |

Dependency versions are centralized in [`gradle/libs.versions.toml`](gradle/libs.versions.toml) and
referenced as `libs.*` in build files rather than hardcoded. Current pins: AGP 9.4.0, Gradle 9.7.1,
Kotlin 2.4.10, Compose Multiplatform 1.12.0, maplibre-compose 0.15.0.

Maps are rendered with [MapLibre](https://maplibre.org/) using
[OpenFreeMap](https://openfreemap.org/) styles (Liberty in light mode, Dark in dark mode) — free, and no
API key required.

## Contributing

Several of the dependencies above are pinned *to each other*, not to "latest", and a few build settings
fail in ways that don't point at themselves. Before upgrading anything or debugging a build, read:

- [`CLAUDE.md`](CLAUDE.md) — the module split, shared tooling, and cross-cutting version pins.
- [`composeApp/CLAUDE.md`](composeApp/CLAUDE.md) — source-set layout, the app shell (tabs, navigation,
  theming, debug gating), and the maplibre/navigation/lifecycle pins.
- [`androidApp/CLAUDE.md`](androidApp/CLAUDE.md) — packaging and edge-to-edge window chrome.
- [`iosApp/CLAUDE.md`](iosApp/CLAUDE.md) — the Xcode settings that are load-bearing.

These files are written as guidance for Claude Code, but they're the project's design notes and are worth
reading whether or not you use it. Machine-specific setup (local SDK paths, simulator and emulator device
names) stays out of version control — keep it in a git-ignored file of your own.
