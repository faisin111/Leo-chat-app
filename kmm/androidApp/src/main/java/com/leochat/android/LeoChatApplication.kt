package com.leochat.android

import android.app.Application
import com.leochat.app.initKoin
import org.koin.android.ext.koin.androidContext

class LeoChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@LeoChatApplication) }
    }
}
