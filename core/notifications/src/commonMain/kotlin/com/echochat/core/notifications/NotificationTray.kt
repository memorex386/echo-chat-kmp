package com.echochat.core.notifications

/** One platform's notifications: Android's tray, iOS's notification center or the Windows tray. */
interface NotificationTray {
    /** Shows Echo's [unread] replies, oldest first. The last one has just arrived. */
    fun show(unread: List<UnreadReply>)

    /** Takes Echo's notifications away, where the platform allows it. */
    fun clear()
}

data class UnreadReply(
    val text: String,
    val receivedAtMillis: Long,
)
