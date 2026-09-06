package org.freefridges.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The map draws under the status bar, so force dark status-bar icons — the
        // default light icons are unreadable over the (always light) map style.
        // Revisit if a dark map style is ever added.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            App(isDebugBuild = BuildConfig.DEBUG)
        }
    }
}
