package com.echochat.core.notifications.di

import com.echochat.core.notifications.ChatNotifier
import com.echochat.core.notifications.ChatRequests
import com.echochat.core.notifications.Notifier
import org.koin.core.module.Module
import org.koin.dsl.module

val notificationsModule = module {
    includes(platformNotificationsModule)
    single { ChatRequests() }
    single<Notifier> { ChatNotifier(get()) }
}

/** Binds each platform's [com.echochat.core.notifications.NotificationTray]. */
internal expect val platformNotificationsModule: Module
