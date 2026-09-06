package org.freefridges.app.navigation

import org.freefridges.app.generated.resources.Res
import org.freefridges.app.generated.resources.ic_tab_debug
import org.freefridges.app.generated.resources.ic_tab_fridge
import org.freefridges.app.generated.resources.ic_tab_map
import org.jetbrains.compose.resources.DrawableResource

/**
 * The tabs in the bottom navigation bar, in display order.
 *
 * [debugOnly] destinations are dropped from both the tab bar and the nav graph in
 * release builds — see `App(isDebugBuild)`.
 */
enum class TopLevelDestination(
    val route: AppRoute,
    val label: String,
    val icon: DrawableResource,
    val debugOnly: Boolean = false
) {
    Map(MapRoute, "Map", Res.drawable.ic_tab_map),
    Fridges(FridgesRoute, "Fridges", Res.drawable.ic_tab_fridge),
    Debug(DebugRoute, "Debug", Res.drawable.ic_tab_debug, debugOnly = true)
}
