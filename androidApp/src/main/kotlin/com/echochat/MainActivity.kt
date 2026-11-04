package com.echochat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.echochat.core.notifications.AndroidNotificationTray
import com.echochat.core.notifications.ChatRequests
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val chatRequests: ChatRequests by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            EchoChatApp()
        }
    }

    // A tap on Echo's notification reaches the running chat here, because the activity is singleTop.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == AndroidNotificationTray.OpenChatAction) chatRequests.request()
    }
}
