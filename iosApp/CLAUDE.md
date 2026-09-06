# iosApp

The Xcode project and SwiftUI shell hosting the Compose UI. Swift code should stay minimal — app logic
belongs in `composeApp`'s `commonMain`/`iosMain`. See the root `CLAUDE.md` for the module split.

## Layout

`iosApp/` holds `iOSApp.swift` and `ContentView.swift`, which put the Compose view controller in a
`UIViewControllerRepresentable`. The Compose side is
`MainViewControllerKt.MainViewController(isDebugBuild:)`, exported from `composeApp/src/iosMain`.

Open `iosApp.xcodeproj` in Xcode to run or debug. The "Compile Kotlin Framework" run-script build phase
invokes `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` automatically, falling back to
`java_home`/Android Studio's JBR if `JAVA_HOME` isn't inherited by the Xcode build environment.

## Settings that are load-bearing

Each of these fails in a way that doesn't point at itself, so check here before debugging from scratch.

- **`OTHER_LDFLAGS` carries MapLibre's system-library flags** (`-l"c++"`, `-lz`, CoreFoundation,
  CoreGraphics, CoreText, Foundation, ImageIO, Metal, QuartzCore), because the Kotlin framework is static
  and those dependencies resolve at app link time. Omitting them still lets
  `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` pass, and fails only at Xcode link time with
  undefined C++/zlib/Metal symbols. They must be present on **both** the Debug and Release configurations.
- **`Info.plist` must keep `CADisableMinimumFrameDurationOnPhone = true`.** Compose UI hard-asserts on it
  at startup, so without it the app throws an uncaught Kotlin exception and dies before drawing a frame —
  and `xcrun simctl launch` without `--console-pty` shows nothing but a return to the home screen.
- **`ContentView.swift` uses `.ignoresSafeArea()`**, not `.ignoresSafeArea(.keyboard)`. Insetting the
  Compose view in SwiftUI stops the Compose canvas above the home indicator, so the navigation bar's
  background stops there too and the white window background shows through as a ~100px gap under the tab
  bar. Compose applies the safe area itself via Scaffold/NavigationBar window insets — let it.
- **Deployment target 15.3**, raised from 15.0 to match the only configuration MapLibre tests.
- **`EXCLUDED_ARCHS[sdk=iphonesimulator*] = x86_64`.** Compose Multiplatform 1.12.0 dropped the Intel
  simulator target and `composeApp` builds no `iosX64`, so this keeps the two in step. Apple-silicon Macs
  only.

## Do not add a MapLibre pod or SPM package

maplibre-compose 0.15.0 ships MapLibre Native inside the Kotlin klib. Pre-0.15 tutorials tell you to add a
`MapLibre` CocoaPods/SwiftPM dependency; doing so conflicts with the bundled static archive. The existing
`embedAndSignAppleFrameworkForXcode` build phase is all the wiring needed.
