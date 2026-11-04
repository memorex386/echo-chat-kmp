package com.echochat.feature.chat

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.platform.PlatformTextInputMethodRequest

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal actual fun isKeyboardUp(): Boolean = WindowInsets.isImeVisible

// Compose asks the keyboard never to go full screen. Lifting that lets a phone keyboard in
// landscape show its own text box and Send button, as Google Messages does. Keyboards stay
// inline in portrait either way.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal actual fun AllowFullScreenKeyboard(content: @Composable () -> Unit) {
    val interceptor = remember {
        PlatformTextInputInterceptor { request, nextHandler ->
            nextHandler.startInputMethod(
                object : PlatformTextInputMethodRequest {
                    override fun createInputConnection(outAttributes: EditorInfo): InputConnection =
                        request.createInputConnection(outAttributes).also {
                            outAttributes.imeOptions = outAttributes.imeOptions and EditorInfo.IME_FLAG_NO_FULLSCREEN.inv()
                        }
                },
            )
        }
    }
    InterceptPlatformTextInput(interceptor, content)
}

// The keyboard's Send key sends; Enter on a hardware keyboard starts a new line, as before.
internal actual fun Modifier.enterSends(canSend: Boolean, onSend: () -> Unit): Modifier = this
