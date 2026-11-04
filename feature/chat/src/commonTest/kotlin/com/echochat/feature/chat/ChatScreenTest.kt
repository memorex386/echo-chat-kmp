package com.echochat.feature.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
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
    private var typed by mutableStateOf("")

    private val sunset = Image("/cache/images/sunset.jpg")

    // The chat is shown in a phone-sized frame, since a desktop test window is wider than tall.
    private fun ComposeUiTest.showChat(
        draft: String = "",
        attachment: Image? = null,
        canSend: Boolean = false,
        onSend: () -> Unit = {},
        onPickPhoto: () -> Unit = {},
        onTakePhoto: (() -> Unit)? = {},
        onRemoveAttachment: () -> Unit = {},
        screen: DpSize = Portrait,
    ) {
        typed = draft
        setContent {
            EchoChatTheme {
                Box(Modifier.size(screen).testTag(ScreenTag)) {
                    ChatScreen(
                        uiState = uiState,
                        draft = typed,
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
    }

    /** A rotation saves the screen's state and rebuilds it from that, shorter. */
    private fun ComposeUiTest.showRotatableChat(): Rotation {
        val rotation = Rotation(this)
        setContent {
            rotation.Screen {
                EchoChatTheme {
                    Box(Modifier.size(Portrait.width, if (rotation.rotated) 220.dp else 460.dp)) {
                        ChatScreen(uiState, typed, null, false, {}, {}, {}, {}, {}, {})
                    }
                }
            }
        }
        return rotation
    }

    /**
     * Compose's StateRestorationTester does this on Android, but isn't implemented on desktop
     * or iOS yet (CMP-7992). [rotated] is a plain var, so only the rebuilt screen sees it.
     */
    private class Rotation(private val test: ComposeUiTest) {
        var rotated = false
        private var shown by mutableStateOf(true)
        private var registry by mutableStateOf(SaveableStateRegistry(null) { true })

        // Saved values are matched by place in the tree, so the screen comes back in the same place.
        @Composable
        fun Screen(content: @Composable () -> Unit) {
            if (shown) CompositionLocalProvider(LocalSaveableStateRegistry provides registry, content = content)
        }

        fun rotate() {
            test.runOnIdle {
                registry = SaveableStateRegistry(registry.performSave()) { true }
                shown = false
            }
            test.runOnIdle {
                rotated = true
                shown = true
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

    @Test
    fun `CHAT-14 a reply that arrives while I'm scrolled up shows a jump arrow instead of moving the list`() = runComposeUiTest {
        uiState = ChatUiState(messages = (1L..40L).map { mine(it, "Message $it") })
        showChat()
        onNode(hasScrollToIndexAction()).performScrollToIndex(0)

        uiState = uiState.copy(messages = uiState.messages + echo(41, "You said: Message 40"))

        // Still at the top, so the list didn't move. The reply, off screen, may not exist at all.
        onNodeWithText("Message 1").assertIsDisplayed()
        onNodeWithContentDescription("Scroll to latest message").assertIsDisplayed().performClick()
        onNodeWithText("You said: Message 40").assertIsDisplayed()
        onNodeWithContentDescription("Scroll to latest message").assertDoesNotExist()
    }

    @Test
    fun `CHAT-14 sending my own message scrolls to it even when I'm scrolled up`() = runComposeUiTest {
        uiState = ChatUiState(messages = (1L..40L).map { mine(it, "Message $it") })
        showChat()
        onNode(hasScrollToIndexAction()).performScrollToIndex(0)

        uiState = uiState.copy(messages = uiState.messages + mine(41, "Message 41"), isReplying = true)

        onNodeWithText("Message 41").assertIsDisplayed()
    }

    @Test
    fun `CHAT-14 after a rotation the list stays where I scrolled to`() = runComposeUiTest {
        uiState = ChatUiState(messages = (1L..40L).map { mine(it, "Message $it") })
        val rotation = showRotatableChat()
        onNode(hasScrollToIndexAction()).performScrollToIndex(0)

        rotation.rotate()

        onNodeWithText("Message 1").assertIsDisplayed()
        onNodeWithContentDescription("Scroll to latest message").assertIsDisplayed()
    }

    @Test
    fun `CHAT-16 in landscape bubbles use at most two thirds of the width`() = runComposeUiTest {
        uiState = ChatUiState(messages = listOf(mine(1, LongMessage), echo(2, "You said: $LongMessage")))
        showChat(screen = Landscape)

        val screen = onNodeWithTag(ScreenTag).getBoundsInRoot().width
        for (text in listOf(LongMessage, "You said: $LongMessage")) {
            val bubbleText = onNodeWithText(text, useUnmergedTree = true).getBoundsInRoot().width
            assertTrue(bubbleText <= screen * 2 / 3, "$bubbleText should be at most 2/3 of $screen")
            assertTrue(bubbleText >= screen / 2, "$bubbleText should still fill most of that")
        }
    }

    @Test
    fun `CHAT-17 a very long message wraps inside its bubble even one long word`() = runComposeUiTest {
        val longWord = "Supercalifragilisticexpialidocious".repeat(6)
        uiState = ChatUiState(messages = listOf(mine(1, longWord), echo(2, LongMessage)))
        showChat()

        val screen = onNodeWithTag(ScreenTag).getBoundsInRoot()
        for (text in listOf(longWord, LongMessage)) {
            val bubbleText = onNodeWithText(text, useUnmergedTree = true).getBoundsInRoot()
            assertTrue(bubbleText.left >= screen.left && bubbleText.right <= screen.right, "$text should stay inside the screen")
            assertTrue(bubbleText.height > 60.dp, "$text should wrap onto several lines")
        }
    }

    @Test
    fun `CHAT-17 the input grows to 4 lines and then scrolls`() = runComposeUiTest {
        showChat(draft = LongMessage.repeat(3), canSend = true)

        val input = onNode(hasSetTextAction()).getBoundsInRoot().height
        assertTrue(input > 80.dp, "The input should grow past one line, but is $input")
        assertTrue(input < 150.dp, "The input should stop at about 4 lines, but is $input")
    }

    @Test
    fun `CHAT-18 the newest message stays in view when the input grows`() = runComposeUiTest {
        uiState = ChatUiState(messages = (1L..40L).map { mine(it, "Message $it") })
        showChat()
        onNodeWithText("Message 40").assertIsDisplayed()

        typed = LongMessage.repeat(3)

        onNodeWithText("Message 40").assertIsDisplayed()
        onNodeWithContentDescription("Scroll to latest message").assertDoesNotExist()
    }

    @Test
    fun `CHAT-18 the end of the newest message stays in view after a rotation even when it is taller than the list`() = runComposeUiTest {
        val tallest = LongMessage.repeat(4)
        uiState = ChatUiState(messages = (1L..40L).map { mine(it, "Message $it") } + echo(41, tallest))
        val rotation = showRotatableChat()
        onNodeWithContentDescription("Scroll to latest message").assertDoesNotExist()

        rotation.rotate()

        // Any part of the message counts as displayed, so this checks where its end is, unclipped.
        val end = onNodeWithText(tallest, useUnmergedTree = true).getUnclippedBoundsInRoot().bottom
        val input = onNode(hasSetTextAction()).getBoundsInRoot().top
        assertTrue(end <= input && end > input - 40.dp, "The message should end just above the input, at $end, not $input")
        onNodeWithContentDescription("Scroll to latest message").assertDoesNotExist()
    }

    @Test
    fun `PHOTO-06 a photo stands on its own and its caption gets a bubble of its own below`() = runComposeUiTest {
        uiState = ChatUiState(messages = listOf(Message(1, Author.Me, "Sunset", sunset)))
        showChat()

        val photo = onNodeWithContentDescription("Open photo from you", useUnmergedTree = true)
        val caption = onNodeWithText("Sunset", useUnmergedTree = true)
        photo.assert(!hasAnyAncestor(hasTestTag(BubbleTestTag)))
        caption.assert(hasAnyAncestor(hasTestTag(BubbleTestTag)))
        assertTrue(caption.getBoundsInRoot().top >= photo.getBoundsInRoot().bottom, "The caption should sit below the photo")
    }

    private fun mine(id: Long, text: String) = Message(id, Author.Me, text)

    private fun echo(id: Long, text: String) = Message(id, Author.Echo, text)

    private val DpRect.centreX get() = (left + right) / 2

    private companion object {
        const val ScreenTag = "screen"
        val Portrait = DpSize(360.dp, 640.dp)
        val Landscape = DpSize(640.dp, 360.dp)
        const val LongMessage = "This is a long message to check wrapping: it keeps going well past the width " +
            "of one line, so the bubble has to wrap it onto several lines without pushing anything off the edge."
    }
}
