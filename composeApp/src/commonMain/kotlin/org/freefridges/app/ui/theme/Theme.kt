package org.freefridges.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Which color scheme the app uses. [System] is the only value a release build can hold —
 * the other two are reachable only from the debug screen, which isn't registered outside
 * debug builds.
 */
enum class ThemeMode(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark");

    companion object {
        /** For `rememberSaveable`; enums aren't saveable out of the box. */
        val Saver: Saver<ThemeMode, Int> = Saver(
            save = { it.ordinal },
            restore = { entries[it] }
        )
    }
}

/**
 * Whether the current [FreeFridgesTheme] is the dark one.
 *
 * Read this rather than calling `isSystemInDarkTheme()` again, so anything that has to
 * pick a light/dark asset itself — the map style, most obviously — follows the theme even
 * when [ThemeMode] has overridden the system setting.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * App theme. Follows the system light/dark setting unless [themeMode] overrides it.
 *
 * The color schemes are the Material 3 baselines for now; a brand palette can be dropped
 * in here without touching any call site.
 */
@Composable
fun FreeFridgesTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    ApplySystemUiTheme(themeMode = themeMode, darkTheme = darkTheme)

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
            content = content
        )
    }
}
