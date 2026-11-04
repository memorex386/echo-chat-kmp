package com.echochat.core.data

import com.echochat.core.database.InMemoryMessageDao
import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import com.echochat.core.network.SimulatedChatService
import com.echochat.core.notifications.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The real in-memory DAO and simulated service, on virtual time: the 5 s delay costs nothing.
// Replies run in the test's own scope, so advanceUntilIdle() delivers them; it skips backgroundScope.
@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {

    private fun TestScope.newRepository(notifier: Notifier = RecordingNotifier()) =
        DefaultChatRepository(InMemoryMessageDao(), SimulatedChatService(), notifier, appScope = this)

    private suspend fun ChatRepository.texts() = messages.first().map { it.text }

    @Test
    fun `NOTIFY-01 every reply is handed to the notifier`() = runTest {
        val notifier = RecordingNotifier()
        val repository = newRepository(notifier)

        repository.send("Hello")
        advanceUntilIdle()

        assertEquals(listOf("You said: Hello"), notifier.replies.map { it.text })
        assertEquals(Author.Echo, notifier.replies.single().author)
    }

    // core:testing can't be used here, because it depends on this module.
    private class RecordingNotifier : Notifier {
        val replies = mutableListOf<Message>()

        override fun notifyReply(reply: Message) {
            replies += reply
        }

        override fun onChatShown() = Unit

        override fun onChatHidden() = Unit
    }

    @Test
    fun `CHAT-01 a sent message becomes the last message and it is mine`() = runTest {
        val repository = newRepository()

        repository.send("Hello")

        val last = repository.messages.first().last()
        assertEquals("Hello", last.text)
        assertEquals(Author.Me, last.author)
    }

    @Test
    fun `CHAT-01 spaces around a message are dropped`() = runTest {
        val repository = newRepository()

        repository.send("  Hello  ")

        assertEquals(listOf("Hello"), repository.texts())
    }

    @Test
    fun `CHAT-04 the echo arrives 5 seconds after sending and not before`() = runTest {
        val repository = newRepository()

        repository.send("Hello")
        advanceTimeBy(4_999)
        runCurrent()
        assertEquals(listOf("Hello"), repository.texts())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("Hello", "You said: Hello"), repository.texts())
        assertEquals(Author.Echo, repository.messages.first().last().author)
    }

    @Test
    fun `CHAT-05 quick sends are each answered in order`() = runTest {
        val repository = newRepository()

        repository.send("A")
        advanceTimeBy(1_000)
        repository.send("B")
        advanceUntilIdle()

        assertEquals(listOf("A", "B", "You said: A", "You said: B"), repository.texts())
    }

    @Test
    fun `CHAT-10 Echo is replying until the last waiting reply arrives`() = runTest {
        val repository = newRepository()
        assertFalse(repository.isReplying.first())

        repository.send("A")
        advanceTimeBy(1_000)
        repository.send("B")
        runCurrent()
        assertTrue(repository.isReplying.first())

        advanceTimeBy(4_000) // A's reply lands at 5 s; B's is still on its way.
        runCurrent()
        assertTrue(repository.isReplying.first())

        advanceTimeBy(1_000)
        runCurrent()
        assertFalse(repository.isReplying.first())
    }

    @Test
    fun `CHAT-12 a reply still arrives after the screen that sent the message is gone`() = runTest {
        val repository = newRepository()
        val screenScope = CoroutineScope(StandardTestDispatcher(testScheduler))

        screenScope.launch { repository.send("Hello") }
        runCurrent()
        screenScope.cancel()
        advanceUntilIdle()

        assertEquals(listOf("Hello", "You said: Hello"), repository.texts())
    }

    @Test
    fun `CHAT-13 emoji survive the round trip`() = runTest {
        val repository = newRepository()

        repository.send("👋🏽")
        advanceUntilIdle()

        assertEquals("You said: 👋🏽", repository.texts().last())
    }

    @Test
    fun `PHOTO-03 a photo and its caption are sent as one message`() = runTest {
        val repository = newRepository()
        val sunset = Image("/cache/images/sunset.jpg")

        repository.send("Sunset", sunset)

        val messages = repository.messages.first()
        assertEquals(1, messages.size)
        assertEquals("Sunset", messages.single().text)
        assertEquals(sunset, messages.single().image)
    }
}
