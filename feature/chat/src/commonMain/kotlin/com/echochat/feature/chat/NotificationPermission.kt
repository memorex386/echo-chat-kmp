package com.echochat.feature.chat

import androidx.compose.runtime.Composable

/**
 * Asks once, on the first send, for permission to show Echo's replies as notifications, where the
 * platform needs it. The first send is when the reason is clear. A refusal is left alone.
 */
@Composable
internal expect fun rememberNotificationPermissionRequest(): () -> Unit
