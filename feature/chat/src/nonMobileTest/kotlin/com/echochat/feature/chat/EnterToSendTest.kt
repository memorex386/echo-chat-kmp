package com.echochat.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import com.echochat.core.designsystem.theme.EchoChatTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import com.echochat.core.testing.loadStrings
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.allStringResources

// Desktop and the web: phones keep their keyboard's own Send key.
@OptIn(ExperimentalTestApi::class)
class EnterToSendTest {

    private var typed by mutableStateOf("")
    private var sent = 0

    private suspend fun ComposeUiTest.showChat(draft: String) {
        typed = draft
        loadStrings(Res.allStringResources)
        setContent {
            EchoChatTheme {
                ChatScreen(
                    uiState = ChatUiState(),
                    draft = typed,
                    attachment = null,
                    canSend = typed.isNotBlank(),
                    onDraftChange = { typed = it },
                    onSend = { sent++ },
                    onPickPhoto = {},
                    onTakePhoto = null,
                    onRemoveAttachment = {},
                    onOpenImage = {},
                )
            }
        }
        onNode(hasSetTextAction()).performClick()
    }

    @Test
    fun `KEY-05 Enter sends the message`() = runComposeUiTest {
        showChat("Hello")

        onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Enter) }

        assertEquals(1, sent)
        assertEquals("Hello", typed, "Enter shouldn't add a line")
    }

    @Test
    fun `KEY-05 Shift+Enter starts a new line instead`() = runComposeUiTest {
        showChat("Hello")

        onNode(hasSetTextAction()).performKeyInput { withKeyDown(Key.ShiftLeft) { pressKey(Key.Enter) } }

        assertEquals(0, sent)
        assertEquals("Hello\n", typed)
    }

    @Test
    fun `KEY-05 Enter with nothing to send adds no empty line`() = runComposeUiTest {
        showChat("")

        onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Enter) }

        assertEquals(0, sent)
        assertEquals("", typed)
    }
}
