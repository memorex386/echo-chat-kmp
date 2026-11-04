package com.echochat

import android.app.Application

class EchoChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
