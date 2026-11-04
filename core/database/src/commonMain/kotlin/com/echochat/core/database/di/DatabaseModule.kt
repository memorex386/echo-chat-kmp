package com.echochat.core.database.di

import com.echochat.core.database.InMemoryMessageDao
import com.echochat.core.database.MessageDao
import org.koin.dsl.module

val databaseModule = module {
    single<MessageDao> { InMemoryMessageDao() }
}
