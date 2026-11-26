package com.leochat.app.di

import com.leochat.core.common.di.commonModule
import com.leochat.core.datastore.di.datastoreModule
import com.leochat.core.designsystem.di.designSystemModule
import com.leochat.core.network.di.httpEngineModule
import com.leochat.core.network.di.networkModule
import com.leochat.core.platform.di.platformModule
import com.leochat.feature.auth.di.authModule
import com.leochat.feature.chat.di.chatModule
import com.leochat.feature.splash.di.splashModule
import com.leochat.feature.users.di.usersModule
import com.leochat.feature.ai.di.aiModule
import com.leochat.feature.calls.di.callsModule
import com.leochat.feature.calls.di.callsPlatformModule
import com.leochat.feature.chat.di.contactsModule
import com.leochat.feature.media.di.mediaModule
import com.leochat.feature.media.di.mediaPlatformModule
import com.leochat.feature.notifications.di.notificationsModule
import com.leochat.feature.settings.di.settingsModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Every Koin module in the app, core first, then features. */
val appModules = listOf(
    platformModule,
    httpEngineModule,
    commonModule,
    networkModule,
    datastoreModule,
    designSystemModule,
    authModule,
    splashModule,
    chatModule,
    usersModule,
    contactsModule,
    aiModule,
    settingsModule,
    mediaModule,
    mediaPlatformModule,
    callsModule,
    callsPlatformModule,
    notificationsModule,
)

/** Starts DI once per process. Android passes `androidContext`; iOS passes nothing. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModules)
    }
}
