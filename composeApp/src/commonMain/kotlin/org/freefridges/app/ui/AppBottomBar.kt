package org.freefridges.app.ui

import androidx.compose.runtime.Composable
import org.freefridges.app.navigation.TopLevelDestination

/**
 * The bottom tab bar.
 *
 * Deliberately platform-specific: Android uses the standard Material3 `NavigationBar`,
 * while iOS uses a compact bar sized and laid out like a native UIKit tab bar. Keep the
 * tab list itself in [TopLevelDestination] so the two only differ in presentation.
 */
@Composable
expect fun AppBottomBar(
    tabs: List<TopLevelDestination>,
    isSelected: (TopLevelDestination) -> Boolean,
    onSelect: (TopLevelDestination) -> Unit
)
