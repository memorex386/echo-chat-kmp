package com.echochat.core.network.di

import com.echochat.core.network.ChatService
import com.echochat.core.network.SimulatedChatService
import org.koin.dsl.module

val networkModule = module {
    factory<ChatService> { SimulatedChatService() }
}
