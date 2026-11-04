package com.echochat.core.notifications.di

import com.echochat.core.notifications.IosNotificationTray
import com.echochat.core.notifications.NotificationTray
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNotificationsModule: Module = module {
    single<NotificationTray> { IosNotificationTray() }
}
