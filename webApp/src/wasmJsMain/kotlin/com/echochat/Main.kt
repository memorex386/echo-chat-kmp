package com.echochat

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport { WebApp() }
}

/** The chat in a browser tab, with the browser's Back button working as the app's Back (D35). */
@Composable
fun WebApp() {
    BrowserBack()
    EchoChatApp()
}
