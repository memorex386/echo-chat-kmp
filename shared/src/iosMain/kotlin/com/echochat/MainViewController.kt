package com.echochat

import androidx.compose.ui.window.ComposeUIViewController
import com.echochat.core.notifications.installNotificationDelegate
import org.koin.mp.KoinPlatform

/** The chat as a UIKit view controller, for the Swift app to show. */
@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController {
    EchoChatApp()
}.also {
    installNotificationDelegate(KoinPlatform.getKoin().get())
}
