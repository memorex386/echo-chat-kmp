package com.echochat.feature.chat

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

@Composable
internal actual fun isKeyboardUp(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0

// iPhone keyboards have no full-screen text box.
@Composable
internal actual fun AllowFullScreenKeyboard(content: @Composable () -> Unit) = content()

// The keyboard's Send key sends; Enter on a hardware keyboard starts a new line.
internal actual fun Modifier.enterSends(canSend: Boolean, onSend: () -> Unit): Modifier = this
