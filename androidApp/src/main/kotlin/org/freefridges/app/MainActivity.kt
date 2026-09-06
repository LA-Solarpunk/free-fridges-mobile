package org.freefridges.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Both bars are drawn on by the app — the map runs under the status bar, and the
        // Material NavigationBar runs under the navigation bar — so keep them fully
        // transparent (no scrim) and let SystemBarStyle.auto pick the icon tint from the
        // night-mode configuration. That is the same signal FreeFridgesTheme follows via
        // isSystemInDarkTheme(), so the icons always contrast with what's behind them.
        // The activity is recreated on a night-mode change, so this re-runs.
        val transparent = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = transparent, navigationBarStyle = transparent)
        super.onCreate(savedInstanceState)
        setContent {
            App(isDebugBuild = BuildConfig.DEBUG)
        }
    }
}
