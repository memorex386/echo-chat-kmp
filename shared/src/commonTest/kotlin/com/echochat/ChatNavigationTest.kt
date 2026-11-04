package com.echochat

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.echochat.core.data.ChatRepository
import com.echochat.core.data.ImageStore
import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import com.echochat.core.testing.TestChatRepository
import com.echochat.core.testing.TestImageStore
import com.echochat.feature.chat.di.chatModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

// The whole app, with fakes behind the chat screen's ViewModel.
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class ChatNavigationTest {

    private val chatRepository = TestChatRepository()

    @BeforeTest
    fun setUp() {
        // The ViewModel and the screen's lifecycle-aware collection run on Dispatchers.Main,
        // which a plain JVM test doesn't have.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        startKoin {
            modules(
                module {
                    single<ChatRepository> { chatRepository }
                    single<ImageStore> { TestImageStore() }
                },
                chatModule,
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `PHOTO-05 tapping a photo opens it full screen and Back returns to the chat`() = runComposeUiTest {
        chatRepository.messages.value = listOf(Message(1, Author.Me, "Sunset", Image("/cache/images/sunset.jpg")))
        setContent { AppTestHost { EchoChatApp() } }

        onNodeWithContentDescription("Open photo from you").performClick()
        onNodeWithContentDescription("Photo from you").assertIsDisplayed()
        onNodeWithText("Sunset").assertDoesNotExist()

        onNodeWithContentDescription("Back").performClick()

        onNodeWithText("Sunset").assertIsDisplayed()
    }
}
