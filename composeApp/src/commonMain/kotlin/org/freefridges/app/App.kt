package org.freefridges.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.freefridges.app.navigation.DebugRoute
import org.freefridges.app.navigation.FridgesRoute
import org.freefridges.app.navigation.MapRoute
import org.freefridges.app.navigation.TopLevelDestination
import org.freefridges.app.ui.AppBottomBar
import org.freefridges.app.ui.DebugScreen
import org.freefridges.app.ui.FridgesScreen
import org.freefridges.app.ui.MapScreen
import org.freefridges.app.ui.theme.FreeFridgesTheme
import org.freefridges.app.ui.theme.ThemeMode

/**
 * Root of the shared UI.
 *
 * [isDebugBuild] is supplied by each platform shell (`BuildConfig.DEBUG` on Android,
 * `#if DEBUG` on iOS) and gates the Debug tab. It defaults to `false` so previews and
 * any future test call sites stay release-shaped.
 */
@Composable
fun App(isDebugBuild: Boolean = false) {
    // Only the debug screen can move this off ThemeMode.System, and that screen has no
    // route in a release build — so a release app always follows the system setting.
    var themeMode by rememberSaveable(stateSaver = ThemeMode.Saver) {
        mutableStateOf(ThemeMode.System)
    }

    FreeFridgesTheme(themeMode = themeMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val tabs = remember(isDebugBuild) {
            TopLevelDestination.entries.filter { !it.debugOnly || isDebugBuild }
        }

        Scaffold(
            bottomBar = {
                AppBottomBar(
                    tabs = tabs,
                    isSelected = { tab ->
                        backStackEntry?.destination?.hasRoute(tab.route::class) == true
                    },
                    onSelect = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MapRoute,
                modifier = Modifier.fillMaxSize()
            ) {
                // The map draws edge-to-edge under the status bar and only avoids the
                // bottom bar. Its overlay controls (attribution, compass, scale bar) are
                // placed from contentWindowInsets, which MaplibreMap reads directly rather
                // than through the modifier chain — so the bottom side has to be dropped
                // here, or the bar's inset lands on the map a second time.
                composable<MapRoute> {
                    MapScreen(
                        modifier = Modifier.padding(
                            bottom = innerPadding.calculateBottomPadding()
                        ),
                        contentWindowInsets = WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                        )
                    )
                }
                composable<FridgesRoute> { FridgesScreen(Modifier.padding(innerPadding)) }
                // Registered only in debug builds, so release has no route to it at all.
                if (isDebugBuild) {
                    composable<DebugRoute> {
                        DebugScreen(
                            themeMode = themeMode,
                            onThemeModeChange = { themeMode = it },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
