package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

// Desktop and desktop browsers have no on-screen keyboard.
@Composable
internal actual fun isKeyboardUp(): Boolean = false

@Composable
internal actual fun AllowFullScreenKeyboard(content: @Composable () -> Unit) = content()

// A computer's keyboard has no Send key, so Enter sends, as in desktop chat apps. With nothing to
// send it does nothing, rather than adding an empty line.
internal actual fun Modifier.enterSends(canSend: Boolean, onSend: () -> Unit): Modifier = onPreviewKeyEvent { event ->
    val enter = event.key == Key.Enter || event.key == Key.NumPadEnter
    if (!enter || event.isShiftPressed) return@onPreviewKeyEvent false
    if (event.type == KeyEventType.KeyDown && canSend) onSend()
    true
}
