package com.echochat.core.notifications

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// Android's own notifications, on the JVM through Robolectric. Robolectric runs Android 16 at most.
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class AndroidNotificationTrayTest {

    private val app: Application = ApplicationProvider.getApplicationContext()
    private val notifications = shadowOf(app.getSystemService(NotificationManager::class.java))
    private val tray = AndroidNotificationTray(app)

    @Before
    fun allowNotifications() {
        shadowOf(app).grantPermissions(POST_NOTIFICATIONS)
    }

    @Test
    fun `NOTIFY-01 a reply is posted as a conversation notification from Echo`() {
        tray.show(listOf(unread("You said: Hello")))

        val conversation = notifications.allNotifications.single().conversation()
        assertEquals(listOf("You said: Hello"), conversation.texts())
        assertEquals("Echo", conversation.messages.single().person?.name)
    }

    @Test
    fun `NOTIFY-02 clearing takes Echo's notification away`() {
        tray.show(listOf(unread("You said: Hello")))

        tray.clear()

        assertTrue(notifications.allNotifications.isEmpty())
    }

    @Test
    fun `NOTIFY-03 replies stack in one conversation notification, oldest first`() {
        tray.show(listOf(unread("You said: A"), unread("You said: B")))

        assertEquals(listOf("You said: A", "You said: B"), notifications.allNotifications.single().conversation().texts())
    }

    @Test
    fun `NOTIFY-04 tapping the notification asks the app to open the chat`() {
        tray.show(listOf(unread("You said: Hello")))

        val tap = shadowOf(notifications.allNotifications.single().contentIntent).savedIntent

        assertEquals(AndroidNotificationTray.OpenChatAction, tap.action)
        assertEquals(app.packageName, tap.`package`)
    }

    @Test
    fun `NOTIFY-05 nothing is posted when notifications were refused`() {
        shadowOf(app).denyPermissions(POST_NOTIFICATIONS)

        tray.show(listOf(unread("You said: Hello")))

        assertTrue(notifications.allNotifications.isEmpty())
    }

    private fun unread(text: String) = UnreadReply(text, receivedAtMillis = 1_000)

    private fun Notification.conversation() = requireNotNull(MessagingStyle.extractMessagingStyleFromNotification(this))

    private fun MessagingStyle.texts() = messages.map { it.text.toString() }
}
