package com.echochat.core.testing

import com.echochat.core.model.Message
import com.echochat.core.notifications.Notifier

/** Records what it is told instead of posting notifications. */
class TestNotifier : Notifier {
    val replies = mutableListOf<Message>()
    var chatShown = false
        private set

    override fun notifyReply(reply: Message) {
        replies += reply
    }

    override fun onChatShown() {
        chatShown = true
    }

    override fun onChatHidden() {
        chatShown = false
    }
}
