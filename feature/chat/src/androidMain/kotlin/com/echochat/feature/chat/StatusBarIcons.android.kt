package com.echochat.feature.chat

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
internal actual fun LightStatusBarIcons() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val insets = WindowCompat.getInsetsController(window, view)
        val wasLight = insets.isAppearanceLightStatusBars
        insets.isAppearanceLightStatusBars = false
        onDispose { insets.isAppearanceLightStatusBars = wasLight }
    }
}
