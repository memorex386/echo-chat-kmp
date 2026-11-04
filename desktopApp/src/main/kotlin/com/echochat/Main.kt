package com.echochat

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Echo Chat",
            state = rememberWindowState(width = 420.dp, height = 760.dp),
        ) {
            EchoChatApp()
        }
    }
}
