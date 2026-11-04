package com.echochat.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Whether the on-screen keyboard is showing. */
@Composable
internal expect fun isKeyboardUp(): Boolean

/**
 * Lets a phone keyboard in landscape use its own full-screen text box, where the platform has
 * one, instead of leaving the chat a thin strip.
 */
@Composable
internal expect fun AllowFullScreenKeyboard(content: @Composable () -> Unit)

/** Where a physical keyboard is the only one, Enter sends and Shift+Enter starts a new line. */
internal expect fun Modifier.enterSends(canSend: Boolean, onSend: () -> Unit): Modifier
