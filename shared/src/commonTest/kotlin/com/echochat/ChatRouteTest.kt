package com.echochat

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.SavedStateHandle
import com.echochat.core.designsystem.theme.EchoChatTheme
import com.echochat.core.testing.TestChatRepository
import com.echochat.core.testing.TestImageStore
import com.echochat.core.testing.TestNotifier
import com.echochat.core.testing.loadStrings
import com.echochat.core.testing.testPhotoFile
import com.echochat.feature.chat.ChatRoute
import com.echochat.feature.chat.ChatViewModel
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.allStringResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The chat screen with its ViewModel and fakes behind it. The system's permission prompt is
// replaced by a count of how often the chat asked for it.
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class ChatRouteTest {

    private val chatRepository = TestChatRepository()
    private val imageStore = TestImageStore()
    private var askedToNotify = 0
    private lateinit var viewModel: ChatViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ChatViewModel(SavedStateHandle(), chatRepository, imageStore, TestNotifier())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun ComposeUiTest.showChat() {
        loadStrings(Res.allStringResources)
        setContent {
            AppTestHost {
                EchoChatTheme {
                    ChatRoute(onOpenImage = {}, viewModel = viewModel, askToNotify = { askedToNotify++ })
                }
            }
        }
    }

    // Closing the prompt without answering leaves the permission undecided, so the platform would ask again.
    @Test
    fun `NOTIFY-05 the chat asks to notify on the first send and not again`() = runComposeUiTest {
        showChat()

        onNode(hasSetTextAction()).performTextInput("Hello")
        onNodeWithContentDescription("Send").performClick()
        onNode(hasSetTextAction()).performTextInput("Again")
        onNodeWithContentDescription("Send").performClick()
        waitForIdle()

        assertEquals(2, chatRepository.sent.size)
        assertEquals(1, askedToNotify)
    }

    @Test
    fun `NOTIFY-05 a Send with nothing to send doesn't ask`() = runComposeUiTest {
        showChat()

        onNode(hasSetTextAction()).performImeAction()
        waitForIdle()

        assertEquals(0, askedToNotify)
    }

    @Test
    fun `PHOTO-07 the chat says when a photo couldn't be attached`() = runComposeUiTest {
        imageStore.importWorks = false
        showChat()

        runOnIdle { viewModel.onPhotoPicked(testPhotoFile("/picker/1.jpg")) }

        onNodeWithText("Couldn't attach the photo").assertIsDisplayed()
    }
}
