package com.leochat.android

import android.app.Application
import com.leochat.android.push.AndroidPushTokenProvider
import com.leochat.android.push.AppForegroundTracker
import com.leochat.android.push.IncomingCallNotificationHelper
import com.leochat.app.di.initKoin
import com.leochat.core.network.push.PushTokenProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val androidPushModule = module {
    single<PushTokenProvider> { AndroidPushTokenProvider() }
}

class LeoChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppForegroundTracker)
        IncomingCallNotificationHelper.ensureChannel(this)
        initKoin {
            androidContext(this@LeoChatApplication)
            modules(androidPushModule)
        }
    }
}
