package com.echochat

import com.echochat.core.data.di.dataModule
import com.echochat.feature.chat.di.chatModule
import org.koin.core.context.startKoin

val appModules = listOf(dataModule, chatModule)

/**
 * Starts dependency injection. Each platform calls this once, when the app starts, so the
 * messages and replies outlive any one screen. iOS calls it from Swift as `KoinKt.doInitKoin()`.
 */
fun initKoin() {
    startKoin { modules(appModules) }
}
