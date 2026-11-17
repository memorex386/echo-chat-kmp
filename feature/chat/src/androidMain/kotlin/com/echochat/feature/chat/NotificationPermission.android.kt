package com.echochat.feature.chat

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
internal actual fun rememberNotificationPermissionRequest(): () -> Unit {
    if (Build.VERSION.SDK_INT < 33) return {}
    val context = LocalContext.current
    val request = rememberLauncherForActivityResult(RequestPermission()) {}
    return {
        if (ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            request.launch(POST_NOTIFICATIONS)
        }
    }
}
