package org.freefridges.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(isDebugBuild: Boolean): UIViewController =
    ComposeUIViewController { App(isDebugBuild = isDebugBuild) }
