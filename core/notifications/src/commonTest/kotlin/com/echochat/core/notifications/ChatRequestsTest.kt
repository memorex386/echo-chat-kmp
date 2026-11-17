package com.echochat.core.notifications

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRequestsTest {

    private val chatRequests = ChatRequests()

    // Android can rebuild the app from a notification tap, with the photo viewer still open, and
    // the tap arrives before the rebuilt chat is listening.
    @Test
    fun `NOTIFY-04 a request made before the chat listens reaches it once it does and only once`() = runTest {
        var answered = 0
        chatRequests.request()

        val first = launch(UnconfinedTestDispatcher(testScheduler)) { chatRequests.requested.collect { answered++ } }
        first.cancel()
        // A later listener, as after a rotation, doesn't get the same request again.
        val second = launch(UnconfinedTestDispatcher(testScheduler)) { chatRequests.requested.collect { answered++ } }
        second.cancel()

        assertEquals(1, answered)
    }
}
