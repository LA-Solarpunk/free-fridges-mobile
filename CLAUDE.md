# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Free Fridges — a Kotlin Multiplatform + Compose Multiplatform app (Android + iOS) for finding and sharing
community fridges. Beyond the app shell (a map tab plus placeholder screens), features are not yet built.

## Where guidance lives

Keep this file to cross-cutting concerns. Everything else has a home:

- **`<module>/CLAUDE.md`** — guidance specific to one module lives in a `CLAUDE.md` homed in that module,
  not here: [`composeApp/CLAUDE.md`](composeApp/CLAUDE.md), [`androidApp/CLAUDE.md`](androidApp/CLAUDE.md),
  [`iosApp/CLAUDE.md`](iosApp/CLAUDE.md). Claude Code loads these automatically when working on files in
  that directory. When you learn something about a single module, add it there.
- **This file** — the module split, shared tooling, and version pins that span more than one module.

Machine-specific setup — local SDK and toolchain paths, simulator and emulator device names — is personal
and stays out of version control entirely. Keep it in a git-ignored file of your own rather than in any
file committed here.

## Commands

`JAVA_HOME` must point at a JDK 17+. On macOS, Android Studio's bundled JBR works:
```
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

- Build Android debug APK: `./gradlew :androidApp:assembleDebug`
- Run all Gradle checks: `./gradlew build`
- Compile the iOS framework only (no Xcode): `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
- Build the iOS app via CLI: `cd iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build`

Running on a simulator or emulator additionally depends on devices created locally on each machine.

There is no test suite yet.

## Architecture

Three modules: a pure KMP library (`composeApp`), a thin Android application shell (`androidApp`), and an
iOS Xcode wrapper (`iosApp`) — split apart because AGP 9 no longer allows `com.android.application` and
`org.jetbrains.kotlin.multiplatform` in the same module (see version pins below).

- **`composeApp`** — all shared Compose UI and logic, plus the `expect`/`actual` implementations that need
  a platform API. Screens and business logic belong here. See [`composeApp/CLAUDE.md`](composeApp/CLAUDE.md).
- **`androidApp`** — plain Android application module: manifest, application id, launcher icon, theme,
  `MainActivity`. Depends on `composeApp`. See [`androidApp/CLAUDE.md`](androidApp/CLAUDE.md).
- **`iosApp`** — SwiftUI shell hosting the Compose UI, and the Xcode project settings. Swift code should
  stay minimal. See [`iosApp/CLAUDE.md`](iosApp/CLAUDE.md).

Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog); reference them via
`libs.*` / `libs.plugins.*` in build files rather than hardcoding versions.

## Version pins (read before upgrading)

- **AGP 9.4.0**, using the `com.android.kotlin.multiplatform.library` plugin for `composeApp` (not
  `com.android.library` — AGP 9 rejects that combined with the KMP plugin) and plain `com.android.application`
  for `androidApp`. AGP 9's built-in Kotlin support means `androidApp` does **not** apply
  `org.jetbrains.kotlin.android` — adding it back will fail the build ("no longer required since AGP 9.0").
- **Gradle 9.7.1** (required by AGP 9.x).
- **Compose Multiplatform 1.12.0**, **Kotlin 2.4.10**, **compileSdk/targetSdk 37**, **minSdk 26**.
- Several dependencies are pinned to each other rather than to "latest" — maplibre-compose, navigation, and
  lifecycle in particular. See `composeApp/CLAUDE.md` before bumping any of them.
- `org.gradle.jvmargs`/`kotlin.daemon.jvmargs` are at **6 GB**: linking the release iOS framework against
  MapLibre Native OOMs at the previous 2 GB. The flip side is that a full `./gradlew build` can claim ~12 GB
  across the two daemons, which is enough for macOS to kill a running Android emulator out from under you —
  shut the emulator down before a clean build, or expect to restart it after.
