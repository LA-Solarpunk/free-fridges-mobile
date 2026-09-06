# androidApp

The Android application shell. Deliberately thin — it owns platform packaging and window chrome, nothing
else. App logic belongs in `composeApp`; see the root `CLAUDE.md` for the module split.

## Layout

- `src/main` uses the ordinary Android layout, **not** KMP's `src/androidMain`. It owns the manifest,
  application id, launcher icon, and theme, and depends on `composeApp` via
  `implementation(project(":composeApp"))`.
- `MainActivity` calls `setContent { App(isDebugBuild = BuildConfig.DEBUG) }` and does nothing else of
  substance. Android-target Kotlin (`expect`/`actual` implementations included) goes in
  `composeApp/src/androidMain`, not here.

## Build configuration

- `buildFeatures { buildConfig = true }` is required. AGP 8+/9 does not generate `BuildConfig` unless
  asked, and `BuildConfig.DEBUG` is what gates the debug-only tab.
- `assembleRelease` produces an *unsigned* APK — there is no `signingConfig`. To run one locally, sign it
  with the debug keystore via `zipalign` + `apksigner`.

## Window chrome and edge-to-edge

`MainActivity` calls `enableEdgeToEdge` with `SystemBarStyle.auto(TRANSPARENT, TRANSPARENT)` for **both**
bars. The app paints under both (the map under the status bar, `NavigationBar` under the navigation bar),
so any scrim would show through; `auto` takes the icon tint from the night-mode configuration, which is the
same signal `FreeFridgesTheme` follows. It is only the *initial* value — `ApplySystemUiTheme` in
`composeApp` re-asserts the tint whenever `ThemeMode` overrides the system.

Leaving `navigationBarStyle` at its default is the bug this replaced: its scrim and dark-mode icon tint are
both wrong against an app-drawn bar.

`src/main/res/values-night/` carries the dark window-background theme so there's no white flash before the
first frame.
