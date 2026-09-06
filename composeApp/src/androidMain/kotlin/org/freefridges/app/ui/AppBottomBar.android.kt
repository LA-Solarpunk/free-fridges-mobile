package org.freefridges.app.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.freefridges.app.navigation.TopLevelDestination
import org.jetbrains.compose.resources.painterResource

/** The standard Material3 navigation bar — the right look on Android. */
@Composable
actual fun AppBottomBar(
    tabs: List<TopLevelDestination>,
    isSelected: (TopLevelDestination) -> Boolean,
    onSelect: (TopLevelDestination) -> Unit
) {
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = isSelected(tab),
                onClick = { onSelect(tab) },
                icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                label = { Text(tab.label) }
            )
        }
    }
}
