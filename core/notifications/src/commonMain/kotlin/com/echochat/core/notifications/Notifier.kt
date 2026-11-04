package com.echochat.core.notifications

import com.echochat.core.model.Message

/** Tells me about Echo's replies when I can't see the chat. */
interface Notifier {
    /** Shows [reply] in a notification, unless the chat is on screen. */
    fun notifyReply(reply: Message)

    /** The chat is on screen: clears Echo's notifications and stays quiet until [onChatHidden]. */
    fun onChatShown()

    /** The chat has left the screen: the app went to the background, or the photo viewer opened. */
    fun onChatHidden()
}
