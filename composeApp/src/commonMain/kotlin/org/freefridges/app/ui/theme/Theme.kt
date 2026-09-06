package org.freefridges.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the current [FreeFridgesTheme] is the dark one.
 *
 * Read this rather than calling `isSystemInDarkTheme()` again, so anything that has to
 * pick a light/dark asset itself — the map style, most obviously — stays in step with the
 * theme even if it is ever forced to a value other than the system's.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/**
 * App theme. Follows the system light/dark setting by default.
 *
 * The color schemes are the Material 3 baselines for now; a brand palette can be dropped
 * in here without touching any call site.
 */
@Composable
fun FreeFridgesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
            content = content
        )
    }
}
