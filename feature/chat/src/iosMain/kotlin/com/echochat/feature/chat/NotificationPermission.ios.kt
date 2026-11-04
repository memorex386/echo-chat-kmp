package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

// iOS shows its prompt only the first time; after that it answers with the earlier choice.
@Composable
internal actual fun rememberNotificationPermissionRequest(): () -> Unit {
    var asked by rememberSaveable { mutableStateOf(false) }
    return {
        if (!asked) {
            asked = true
            UNUserNotificationCenter.currentNotificationCenter()
                .requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { _, _ -> }
        }
    }
}
