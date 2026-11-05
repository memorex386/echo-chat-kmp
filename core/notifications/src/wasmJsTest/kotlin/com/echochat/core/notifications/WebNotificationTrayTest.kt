package com.echochat.core.notifications

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WebNotificationTrayTest {

    private val browser = FakeWebNotifications()
    private val chatRequests = ChatRequests()
    private val tray = WebNotificationTray(chatRequests, browser)

    @Test
    fun `NOTIFY-03 each reply gets a notification of its own from Echo`() {
        tray.show(listOf(reply("One")))
        tray.show(listOf(reply("One"), reply("Two")))

        assertEquals(listOf("Echo" to "One", "Echo" to "Two"), browser.shown.map { it.title to it.body })
    }

    @Test
    fun `NOTIFY-02 clearing closes every notification the tray showed`() {
        tray.show(listOf(reply("One")))
        tray.show(listOf(reply("One"), reply("Two")))

        tray.clear()

        assertEquals(listOf(true, true), browser.shown.map { it.closed })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `NOTIFY-04 clicking a notification asks for the chat`() = runTest {
        var asked = false
        launch(UnconfinedTestDispatcher(testScheduler)) {
            chatRequests.requested.first()
            asked = true
        }
        tray.show(listOf(reply("One")))

        browser.shown.single().click()

        assertTrue(asked)
    }

    @Test
    fun `NOTIFY-05 without permission nothing is shown`() {
        browser.allowed = false

        tray.show(listOf(reply("One")))

        assertEquals(emptyList(), browser.shown)
    }

    private fun reply(text: String) = UnreadReply(text, receivedAtMillis = 0)
}

private class FakeWebNotifications : WebNotifications {
    override var allowed = true
    val shown = mutableListOf<Shown>()

    override fun show(title: String, body: String, onClick: () -> Unit): ShownNotification =
        Shown(title, body, onClick).also { shown += it }

    class Shown(val title: String, val body: String, val click: () -> Unit) : ShownNotification {
        var closed = false

        override fun close() {
            closed = true
        }
    }
}
