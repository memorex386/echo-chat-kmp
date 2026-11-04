package com.echochat

import com.echochat.core.data.di.dataModule
import com.echochat.feature.chat.di.chatModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

val appModules = listOf(dataModule, chatModule)

/**
 * Starts dependency injection. Each platform calls this once, when the app starts, so the
 * messages and replies outlive any one screen. Android adds its context through [platform].
 * iOS calls it from Swift as `KoinKt.doInitKoin()`.
 */
fun initKoin(platform: KoinAppDeclaration) {
    startKoin {
        platform()
        modules(appModules)
    }
}

// Swift can't use Kotlin's default arguments, so this overload stands in for one.
fun initKoin() = initKoin {}
