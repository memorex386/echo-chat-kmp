package com.echochat.feature.chat

import androidx.compose.runtime.Composable

// The browser asks once. Firefox and Safari only let a page ask straight after a click, and
// this runs as Send is pressed.
@Composable
internal actual fun rememberNotificationPermissionRequest(): () -> Unit = ::askToNotify

@OptIn(ExperimentalWasmJsInterop::class)
private fun askToNotify(): Unit =
    js("typeof Notification !== 'undefined' && Notification.permission === 'default' && Notification.requestPermission()")
