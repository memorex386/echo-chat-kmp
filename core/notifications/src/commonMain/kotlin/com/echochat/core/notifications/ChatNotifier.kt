package com.echochat.core.notifications

import com.echochat.core.model.Message
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.time.Clock

/** Keeps Echo's replies that arrive while the chat is off screen, and shows them in the platform's [tray]. */
class ChatNotifier(
    private val tray: NotificationTray,
) : Notifier {

    // Replies arrive on a background thread while the screen reports from the main one.
    private val lock = SynchronizedObject()
    private val unread = mutableListOf<UnreadReply>()
    private var chatShown = false

    override fun notifyReply(reply: Message) = synchronized(lock) {
        if (chatShown) return@synchronized
        unread += UnreadReply(reply.text, Clock.System.now().toEpochMilliseconds())
        tray.show(unread.toList())
    }

    override fun onChatShown() = synchronized(lock) {
        chatShown = true
        unread.clear()
        tray.clear()
    }

    override fun onChatHidden() = synchronized(lock) {
        chatShown = false
    }
}
