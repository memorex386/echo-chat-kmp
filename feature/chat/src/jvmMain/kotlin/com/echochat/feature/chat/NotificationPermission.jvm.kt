package com.echochat.feature.chat

import androidx.compose.runtime.Composable

// Desktop notifications need no permission.
@Composable
internal actual fun rememberNotificationPermissionRequest(): () -> Unit = {}
