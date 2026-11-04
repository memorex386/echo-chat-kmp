package com.echochat.core.notifications

import kotlinx.cinterop.ObjCSignatureOverride
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

/**
 * iOS's notification center. Each reply is a notification of its own, and sharing a thread
 * groups them into one Echo stack, newest on top, as Messages does.
 */
class IosNotificationTray : NotificationTray {

    // Looked up only when used: test runs have no app bundle, which the notification center needs.
    private val center: UNUserNotificationCenter
        get() = UNUserNotificationCenter.currentNotificationCenter()

    override fun show(unread: List<UnreadReply>) {
        val reply = unread.last()
        val content = UNMutableNotificationContent().apply {
            setTitle(EchoName)
            setBody(reply.text)
            setThreadIdentifier(EchoThread)
            setSound(UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "reply-${reply.receivedAtMillis}-${unread.size}",
            content = content,
            trigger = null,
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override fun clear() {
        center.removeAllDeliveredNotifications()
    }

    private companion object {
        const val EchoName = "Echo"
        const val EchoThread = "echo"
    }
}

/**
 * Lets Echo's notifications show while the app is open, as over the photo viewer, and turns a
 * tap on one into a [ChatRequests] request. The app installs it as it starts.
 */
fun installNotificationDelegate(chatRequests: ChatRequests) {
    val delegate = NotificationDelegate(chatRequests)
    installedDelegate = delegate
    UNUserNotificationCenter.currentNotificationCenter().delegate = delegate
}

// The notification center only holds its delegate weakly.
private var installedDelegate: NotificationDelegate? = null

private class NotificationDelegate(
    private val chatRequests: ChatRequests,
) : NSObject(), UNUserNotificationCenterDelegateProtocol {

    @ObjCSignatureOverride
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit,
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionList or UNNotificationPresentationOptionSound,
        )
    }

    @ObjCSignatureOverride
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit,
    ) {
        chatRequests.request()
        withCompletionHandler()
    }
}
