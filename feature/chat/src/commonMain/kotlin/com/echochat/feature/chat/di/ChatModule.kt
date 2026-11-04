package com.echochat.feature.chat.di

import com.echochat.feature.chat.ChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatModule = module {
    viewModelOf(::ChatViewModel)
}
