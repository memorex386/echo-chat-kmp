package com.echochat.core.notifications

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage

/**
 * Windows notifications. Java can only show one from an icon in the system tray, so the first
 * notification adds Echo's icon there. Clicking a notification asks for the chat.
 */
class DesktopNotificationTray(
    private val chatRequests: ChatRequests,
) : NotificationTray {

    private var icon: TrayIcon? = null

    @Synchronized
    override fun show(unread: List<UnreadReply>) {
        trayIcon()?.displayMessage(EchoName, unread.last().text, TrayIcon.MessageType.NONE)
    }

    // Windows keeps a notification once it has shown it; Java can't take it back.
    override fun clear() = Unit

    private fun trayIcon(): TrayIcon? {
        icon?.let { return it }
        if (!SystemTray.isSupported()) return null
        return TrayIcon(echoAvatar(), "Echo Chat").apply {
            isImageAutoSize = true
            addActionListener { chatRequests.request() }
            SystemTray.getSystemTray().add(this)
            icon = this
        }
    }

    /** Echo's avatar, drawn rather than shipped as a file: an orange circle with "E". */
    private fun echoAvatar() = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB).apply {
        with(createGraphics()) {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            color = Color(0xFF, 0xDC, 0xC2)
            fillOval(0, 0, 64, 64)
            color = Color(0x6E, 0x39, 0x00)
            font = Font(Font.SANS_SERIF, Font.BOLD, 38)
            val metrics = fontMetrics
            drawString("E", (64 - metrics.stringWidth("E")) / 2, (64 - metrics.height) / 2 + metrics.ascent)
            dispose()
        }
    }

    private companion object {
        const val EchoName = "Echo"
    }
}
