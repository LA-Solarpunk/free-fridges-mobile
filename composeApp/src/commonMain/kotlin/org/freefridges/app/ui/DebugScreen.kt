package org.freefridges.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.freefridges.app.ui.theme.ThemeMode

/**
 * Debug-build-only settings. `App(isDebugBuild)` skips registering the route entirely in
 * release builds, so nothing here ships to users — hang new debug switches off this screen
 * rather than inventing another gating mechanism.
 */
@Composable
fun DebugScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Debug", style = MaterialTheme.typography.headlineMedium)
        Text("Debug builds only.", style = MaterialTheme.typography.bodyMedium)

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        ThemeModePicker(themeMode = themeMode, onThemeModeChange = onThemeModeChange)
        Text(
            "Overrides the system light/dark setting for the whole app, including the " +
                "map style and the system bar icons.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModePicker(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == themeMode,
                onClick = { onThemeModeChange(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size)
            ) {
                Text(mode.label)
            }
        }
    }
}
