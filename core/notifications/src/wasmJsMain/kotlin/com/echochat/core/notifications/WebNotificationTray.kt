package com.echochat.core.notifications

/**
 * A browser's notifications. Each reply gets its own, as on Windows, and unlike Windows the page
 * can take them back. Clicking one brings the chat's tab forward and asks for the chat.
 */
class WebNotificationTray(
    private val chatRequests: ChatRequests,
    private val notifications: WebNotifications,
) : NotificationTray {

    private val shown = mutableListOf<ShownNotification>()

    override fun show(unread: List<UnreadReply>) {
        if (!notifications.allowed) return
        shown += notifications.show(EchoName, unread.last().text, onClick = chatRequests::request)
    }

    override fun clear() {
        shown.forEach { it.close() }
        shown.clear()
    }

    private companion object {
        const val EchoName = "Echo"
    }
}
