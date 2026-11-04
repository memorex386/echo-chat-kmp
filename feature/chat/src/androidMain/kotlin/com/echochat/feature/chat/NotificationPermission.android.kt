package com.echochat.feature.chat

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
internal actual fun rememberNotificationPermissionRequest(): () -> Unit {
    if (Build.VERSION.SDK_INT < 33) return {}
    val context = LocalContext.current
    var asked by rememberSaveable { mutableStateOf(false) }
    val request = rememberLauncherForActivityResult(RequestPermission()) {}
    return {
        if (!asked && ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            asked = true
            request.launch(POST_NOTIFICATIONS)
        }
    }
}
