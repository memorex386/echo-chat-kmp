package com.echochat

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.echochat.core.data.ChatRepository
import com.echochat.core.data.ImageStore
import com.echochat.core.notifications.Notifier
import com.echochat.core.testing.TestClipboard
import com.echochat.core.testing.TestImageStore
import com.echochat.core.testing.TestNotifier
import com.echochat.core.testing.plainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import com.echochat.core.testing.loadStrings
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.allStringResources

// The whole app with the real repository, so a delete and its Undo go all the way through.
// Only the clipboard, the image store and the notifier are fakes.
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class MessageActionsTest {

    private val clipboard = TestClipboard()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        startKoin {
            allowOverride(true)
            modules(
                appModules + module {
                    single<ImageStore> { TestImageStore() }
                    single<Notifier> { TestNotifier() }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    // The in-memory store doesn't suspend, so the message is there when this returns.
    private fun send(text: String) {
        CoroutineScope(Dispatchers.Unconfined).launch { KoinPlatform.getKoin().get<ChatRepository>().send(text) }
    }

    private suspend fun ComposeUiTest.showApp() {
        loadStrings(Res.allStringResources)
        setContent {
            CompositionLocalProvider(LocalClipboard provides clipboard) {
                AppTestHost { EchoChatApp() }
            }
        }
    }

    @Test
    fun `ACTION-02 Copy puts the message's text on the clipboard`() = runComposeUiTest {
        send("Copy me")
        showApp()

        onNodeWithText("Copy me").performTouchInput { longClick() }
        onNodeWithText("Copy").performClick()
        waitForIdle()

        assertEquals("Copy me", clipboard.entry?.plainText())
        // These tests run on desktop and iOS, which don't confirm a copy themselves.
        onNodeWithText("Copied").assertIsDisplayed()
    }

    // A browser refuses a copy when the page lacks focus, or Safari when it isn't straight after a click.
    @Test
    fun `ACTION-02 a copy the system refuses doesn't say Copied and the app carries on`() = runComposeUiTest {
        clipboard.refuses = true
        send("Copy me")
        showApp()

        onNodeWithText("Copy me").performTouchInput { longClick() }
        onNodeWithText("Copy").performClick()
        waitForIdle()

        onNodeWithText("Copied").assertDoesNotExist()
        onNodeWithText("Copy me").performTouchInput { longClick() }
        onNodeWithText("Delete").assertIsDisplayed()
    }

    @Test
    fun `ACTION-05 Delete removes the message and Undo brings it back`() = runComposeUiTest {
        send("Oops")
        showApp()

        onNodeWithText("Oops").performTouchInput { longClick() }
        onNodeWithText("Delete").performClick()

        onNodeWithText("Oops").assertDoesNotExist()
        onNodeWithText("Message deleted").assertIsDisplayed()

        onNodeWithText("Undo").performClick()

        onNodeWithText("Oops").assertIsDisplayed()
    }

    @Test
    fun `ACTION-05 the offer to undo goes away on its own after a few seconds`() = runComposeUiTest {
        send("Oops")
        showApp()
        onNodeWithText("Oops").performTouchInput { longClick() }
        onNodeWithText("Delete").performClick()
        onNodeWithText("Message deleted").assertIsDisplayed()

        mainClock.autoAdvance = false
        mainClock.advanceTimeBy(5_000)

        onNodeWithText("Message deleted").assertDoesNotExist()
        onNodeWithText("Oops").assertDoesNotExist()
    }
}
