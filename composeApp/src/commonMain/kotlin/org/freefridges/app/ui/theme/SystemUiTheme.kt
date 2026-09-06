package org.freefridges.app.ui.theme

import androidx.compose.runtime.Composable

/**
 * Points the platform's own chrome at the app theme — the status/navigation bar icon tint,
 * which neither Compose nor [FreeFridgesTheme] can set on its own. Both platforms default
 * it from the OS light/dark setting, so it has to be corrected whenever [ThemeMode]
 * overrides that setting, or the icons end up drawn in the wrong color over the map.
 *
 * Takes the mode as well as the resolved [darkTheme] because [ThemeMode.System] means
 * "hand control back to the OS", which is not the same as pinning the current value.
 */
@Composable
expect fun ApplySystemUiTheme(themeMode: ThemeMode, darkTheme: Boolean)
