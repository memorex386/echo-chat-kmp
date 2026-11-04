package com.echochat.feature.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import com.echochat.core.designsystem.component.AvatarTestTag
import com.echochat.core.designsystem.theme.EchoChatTheme
import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import kotlin.test.Test
import kotlin.test.assertTrue

// Runs on the desktop JVM here, and on the iOS simulator on a Mac.
@OptIn(ExperimentalTestApi::class)
class ChatScreenTest {

    private var uiState by mutableStateOf(ChatUiState())

    private val sunset = Image("/cache/images/sunset.jpg")

    private fun ComposeUiTest.showChat(
        draft: String = "",
        attachment: Image? = null,
        canSend: Boolean = false,
        onSend: () -> Unit = {},
        onPickPhoto: () -> Unit = {},
        onTakePhoto: (() -> Unit)? = {},
        onRemoveAttachment: () -> Unit = {},
    ) {
        setContent {
            EchoChatTheme {
                ChatScreen(
                    uiState = uiState,
                    draft = draft,
                    attachment = attachment,
                    canSend = canSend,
                    onDraftChange = {},
                    onSend = onSend,
                    onPickPhoto = onPickPhoto,
                    onTakePhoto = onTakePhoto,
                    onRemoveAttachment = onRemoveAttachment,
                    onOpenImage = {},
                )
            }
        }
    }

    @Test
    fun `CHAT-02 Send is disabled when there is nothing to send`() = runComposeUiTest {
        showChat(draft = "   ", canSend = false)

        onNodeWithContentDescription("Send").assertIsNotEnabled()
    }

    @Test
    fun `CHAT-02 Send is enabled when there is something to send`() = runComposeUiTest {
        var sent = false
        showChat(draft = "Hello", canSend = true, onSend = { sent = true })

        onNodeWithContentDescription("Send").assertIsEnabled().performClick()

        assertTrue(sent)
    }

    @Test
    fun `CHAT-06 my messages sit on the right and Echo's on the left`() = runComposeUiTest {
        uiState = ChatUiState(messages = listOf(mine(1, "Hello"), echo(2, "You said: Hello")))
        showChat()

        val centre = onRoot().getBoundsInRoot().centreX
        val mine = onNodeWithText("Hello", useUnmergedTree = true).getBoundsInRoot()
        val reply = onNodeWithText("You said: Hello", useUnmergedTree = true).getBoundsInRoot()

        assertTrue(mine.centreX > centre, "My message should be right of centre")
        assertTrue(reply.centreX < centre, "Echo's reply should be left of centre")
    }

    @Test
    fun `CHAT-07 the list scrolls to show a new message`() = runComposeUiTest {
        uiState = ChatUiState(messages = (1L..40L).map { mine(it, "Message $it") })
        showChat()
        onNodeWithText("Message 40").assertIsDisplayed()

        uiState = uiState.copy(messages = uiState.messages + echo(41, "You said: Message 40"))

        onNodeWithText("You said: Message 40").assertIsDisplayed()
    }

    @Test
    fun `CHAT-09 an empty chat says replies arrive after 5 seconds`() = runComposeUiTest {
        showChat()

        onNodeWithText("Send a message and Echo replies 5 seconds later.").assertIsDisplayed()
    }

    @Test
    fun `CHAT-10 typing dots show only while a reply is on its way`() = runComposeUiTest {
        uiState = ChatUiState(messages = listOf(mine(1, "Hello")), isReplying = true)
        showChat()
        onNodeWithContentDescription("Echo is typing").assertIsDisplayed()

        uiState = ChatUiState(messages = listOf(mine(1, "Hello"), echo(2, "You said: Hello")))

        onNodeWithContentDescription("Echo is typing").assertDoesNotExist()
    }

    @Test
    fun `CHAT-11 Echo's name and avatar head the chat and mark each reply`() = runComposeUiTest {
        uiState = ChatUiState(
            messages = listOf(mine(1, "Hi"), echo(2, "You said: Hi"), mine(3, "Again"), echo(4, "You said: Again")),
            isReplying = true,
        )
        showChat()

        onNodeWithText("Echo").assertIsDisplayed()
        // One in the top bar, one beside each of the two replies and one beside the typing dots.
        onAllNodesWithTag(AvatarTestTag, useUnmergedTree = true).assertCountEquals(4)
    }

    @Test
    fun `PHOTO-01 the attach button offers the gallery and the camera`() = runComposeUiTest {
        var picked = false
        var took = false
        showChat(onPickPhoto = { picked = true }, onTakePhoto = { took = true })

        onNodeWithContentDescription("Attach").performClick()
        onNodeWithText("Gallery").performClick()
        onNodeWithContentDescription("Attach").performClick()
        onNodeWithText("Camera").performClick()

        assertTrue(picked, "Gallery should open the photo picker")
        assertTrue(took, "Camera should open the camera")
    }

    @Test
    fun `PHOTO-01 without a camera only the gallery is offered`() = runComposeUiTest {
        showChat(onTakePhoto = null)

        onNodeWithContentDescription("Attach").performClick()

        onNodeWithText("Gallery").assertIsDisplayed()
        onNodeWithText("Camera").assertDoesNotExist()
    }

    @Test
    fun `PHOTO-01 an attached photo shows above the input with a remove button`() = runComposeUiTest {
        showChat(attachment = sunset, canSend = true)

        onNodeWithContentDescription("Remove photo").assertIsDisplayed()
        onNodeWithContentDescription("Send").assertIsEnabled()
    }

    @Test
    fun `PHOTO-02 pressing remove drops the photo`() = runComposeUiTest {
        var removed = false
        showChat(attachment = sunset, canSend = true, onRemoveAttachment = { removed = true })

        onNodeWithContentDescription("Remove photo").performClick()

        assertTrue(removed)
    }

    private fun mine(id: Long, text: String) = Message(id, Author.Me, text)

    private fun echo(id: Long, text: String) = Message(id, Author.Echo, text)

    private val DpRect.centreX get() = (left + right) / 2
}
