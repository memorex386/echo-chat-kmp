package com.echochat.core.notifications

import com.echochat.core.model.Author
import com.echochat.core.model.Message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// When to notify is shared; each platform's tray only posts and clears.
class ChatNotifierTest {

    private val tray = RecordingTray()
    private val notifier = ChatNotifier(tray)

    @Test
    fun `NOTIFY-01 a reply while the chat is hidden is shown in the tray`() {
        notifier.onChatHidden()

        notifier.notifyReply(reply(1, "You said: Hello"))

        assertEquals(listOf(listOf("You said: Hello")), tray.shown)
    }

    @Test
    fun `NOTIFY-02 a reply while the chat is on screen shows nothing`() {
        notifier.onChatShown()

        notifier.notifyReply(reply(1, "You said: Hello"))

        assertTrue(tray.shown.isEmpty())
    }

    @Test
    fun `NOTIFY-02 showing the chat clears Echo's notifications`() {
        notifier.onChatHidden()
        notifier.notifyReply(reply(1, "You said: Hello"))

        notifier.onChatShown()

        assertTrue(tray.cleared)
    }

    @Test
    fun `NOTIFY-03 replies while I'm away are shown together oldest first`() {
        notifier.onChatHidden()

        notifier.notifyReply(reply(1, "You said: A"))
        notifier.notifyReply(reply(2, "You said: B"))

        assertEquals(listOf("You said: A", "You said: B"), tray.shown.last())
    }

    @Test
    fun `NOTIFY-03 the replies start again once the chat has been seen`() {
        notifier.onChatHidden()
        notifier.notifyReply(reply(1, "You said: A"))
        notifier.onChatShown()
        notifier.onChatHidden()

        notifier.notifyReply(reply(2, "You said: B"))

        assertEquals(listOf("You said: B"), tray.shown.last())
    }

    private fun reply(id: Long, text: String) = Message(id, Author.Echo, text)

    private class RecordingTray : NotificationTray {
        val shown = mutableListOf<List<String>>()
        var cleared = false

        override fun show(unread: List<UnreadReply>) {
            shown += unread.map { it.text }
        }

        override fun clear() {
            cleared = true
        }
    }
}
