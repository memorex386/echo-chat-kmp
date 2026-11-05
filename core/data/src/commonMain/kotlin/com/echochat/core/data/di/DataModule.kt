package com.echochat.core.data.di

import com.echochat.core.data.ChatRepository
import com.echochat.core.data.DefaultChatRepository
import com.echochat.core.data.ImageStore
import com.echochat.core.data.platformImageStore
import com.echochat.core.database.di.databaseModule
import com.echochat.core.network.di.networkModule
import com.echochat.core.notifications.di.notificationsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** A scope that lives as long as the app, for work that must outlast any one screen. */
val ApplicationScope = named("ApplicationScope")

// Includes the storage, service and notification modules, so the app needs only this one.
val dataModule = module {
    includes(databaseModule, networkModule, notificationsModule)
    single<CoroutineScope>(ApplicationScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<ChatRepository> { DefaultChatRepository(get(), get(), get(), get(ApplicationScope)) }
    single<ImageStore> { platformImageStore() }
}
