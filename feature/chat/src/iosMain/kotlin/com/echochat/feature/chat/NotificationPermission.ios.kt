package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

// iOS shows its prompt only the first time; after that it answers with the earlier choice.
@Composable
internal actual fun rememberNotificationPermissionRequest(): () -> Unit = ::askToNotify

private fun askToNotify() {
    UNUserNotificationCenter.currentNotificationCenter()
        .requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { _, _ -> }
}
