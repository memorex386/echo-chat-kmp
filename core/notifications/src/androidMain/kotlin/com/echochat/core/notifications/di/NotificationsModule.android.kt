package com.echochat.core.notifications.di

import com.echochat.core.notifications.AndroidNotificationTray
import com.echochat.core.notifications.NotificationTray
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformNotificationsModule: Module = module {
    single<NotificationTray> { AndroidNotificationTray(androidContext()) }
}
