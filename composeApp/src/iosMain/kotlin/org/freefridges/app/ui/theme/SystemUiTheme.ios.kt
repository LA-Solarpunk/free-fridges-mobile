package org.freefridges.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/**
 * The status bar's text color comes from the window's trait collection, so overriding the
 * window's interface style is what makes iOS agree with [ThemeMode] — and it keeps
 * `isSystemInDarkTheme()` consistent with the theme for free. [ThemeMode.System] must map
 * to `Unspecified` rather than the current value, or the trait stops tracking the OS.
 */
@Composable
actual fun ApplySystemUiTheme(themeMode: ThemeMode, darkTheme: Boolean) {
    val style = when (themeMode) {
        ThemeMode.System -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
        ThemeMode.Light -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
        ThemeMode.Dark -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
    }
    SideEffect {
        keyWindow()?.setOverrideUserInterfaceStyle(style)
    }
}

private fun keyWindow(): UIWindow? =
    UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstNotNullOfOrNull { it.keyWindow }
