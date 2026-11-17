package com.echochat.feature.chat

import androidx.compose.runtime.Composable

/**
 * Asks for permission to show Echo's replies as notifications, where the platform needs it. The
 * chat asks once, on the first send, when the reason is clear. A refusal is left alone.
 */
@Composable
internal expect fun rememberNotificationPermissionRequest(): () -> Unit
