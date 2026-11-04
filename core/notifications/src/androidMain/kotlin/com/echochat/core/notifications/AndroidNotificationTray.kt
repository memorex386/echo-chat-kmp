package com.echochat.core.notifications

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat

/** Android's tray: Echo's unread replies as one conversation notification, on a channel that pops up. */
class AndroidNotificationTray(
    private val context: Context,
) : NotificationTray {

    private val tray = NotificationManagerCompat.from(context)

    override fun show(unread: List<UnreadReply>) {
        // A refusal is respected: without the permission, nothing is posted.
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
            return
        }
        tray.createNotificationChannel(
            NotificationChannelCompat.Builder(ChannelId, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.notification_channel))
                .build(),
        )
        tray.notify(NotificationId, conversation(unread))
    }

    override fun clear() {
        tray.cancel(NotificationId)
    }

    private fun conversation(unread: List<UnreadReply>): Notification {
        val echo = Person.Builder().setKey("echo").setName(context.getString(R.string.notification_echo)).build()
        val me = Person.Builder().setName(context.getString(R.string.notification_you)).build()
        val style = NotificationCompat.MessagingStyle(me)
        unread.forEach { style.addMessage(it.text, it.receivedAtMillis, echo) }
        return NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(style)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(openChat())
            .setAutoCancel(true)
            .build()
    }

    /** Brings the app forward and asks it to show the chat, closing the photo viewer if it's open. */
    private fun openChat(): PendingIntent {
        val intent = Intent(OpenChatAction)
            .setPackage(context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        /** The action of the intent a notification opens, so the app knows to show the chat. */
        const val OpenChatAction = "com.echochat.action.OPEN_CHAT"

        private const val ChannelId = "replies"
        private const val NotificationId = 1
    }
}
