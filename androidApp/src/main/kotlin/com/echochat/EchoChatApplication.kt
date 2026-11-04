package com.echochat

import android.app.Application
import org.koin.android.ext.koin.androidContext

class EchoChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@EchoChatApplication) }
    }
}
