package com.echochat

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.vinceglb.filekit.FileKit

fun main() {
    // Names the app's folders, such as the cache that holds photos: Android and iOS have their own.
    FileKit.init(appId = "EchoChat")
    initKoin()
    application {
        val windowState = rememberWindowState(width = 420.dp, height = 760.dp)
        Window(
            onCloseRequest = ::exitApplication,
            title = "Echo Chat",
            state = windowState,
        ) {
            // Clicking one of Echo's notifications brings the chat window forward.
            EchoChatApp(
                onChatRequested = {
                    windowState.isMinimized = false
                    window.toFront()
                    window.requestFocus()
                },
            )
        }
    }
}
