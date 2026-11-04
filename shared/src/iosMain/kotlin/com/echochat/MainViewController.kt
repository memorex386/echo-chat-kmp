package com.echochat

import androidx.compose.ui.window.ComposeUIViewController

/** The chat as a UIKit view controller, for the Swift app to show. */
@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController { EchoChatApp() }
