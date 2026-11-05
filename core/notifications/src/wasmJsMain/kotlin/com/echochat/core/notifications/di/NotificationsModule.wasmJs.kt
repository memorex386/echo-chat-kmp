package com.echochat.core.notifications.di

import com.echochat.core.notifications.BrowserNotifications
import com.echochat.core.notifications.NotificationTray
import com.echochat.core.notifications.WebNotificationTray
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNotificationsModule: Module = module {
    single<NotificationTray> { WebNotificationTray(get(), BrowserNotifications) }
}
