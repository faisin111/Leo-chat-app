package com.leochat.app

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.mp.KoinPlatformTools
import platform.UIKit.UIViewController

/** Entry point for the SwiftUI host: `MainViewControllerKt.MainViewController()`. */
fun MainViewController(): UIViewController {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) initKoin()
    return ComposeUIViewController { LeoChatApp() }
}
