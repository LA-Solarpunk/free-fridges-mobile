package org.freefridges.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

/**
 * Root of the shared UI.
 *
 * [isDebugBuild] is supplied by each platform shell (`BuildConfig.DEBUG` on Android,
 * `#if DEBUG` on iOS) and gates the Debug tab. It defaults to `false` so previews and
 * any future test call sites stay release-shaped.
 */
@Composable
fun App(isDebugBuild: Boolean = false) {
    MaterialTheme {
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
                // bottom bar; MaplibreMap keeps its own overlay controls (attribution,
                // compass, scale bar) inside the safe area via contentWindowInsets.
                composable<MapRoute> {
                    MapScreen(Modifier.padding(bottom = innerPadding.calculateBottomPadding()))
                }
                composable<FridgesRoute> { FridgesScreen(Modifier.padding(innerPadding)) }
                // Registered only in debug builds, so release has no route to it at all.
                if (isDebugBuild) {
                    composable<DebugRoute> { DebugScreen(Modifier.padding(innerPadding)) }
                }
            }
        }
    }
}
