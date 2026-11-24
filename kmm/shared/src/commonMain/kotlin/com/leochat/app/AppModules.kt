package com.leochat.app

import com.leochat.core.di.coreModule
import com.leochat.core.di.platformModule
import com.leochat.features.auth.presentation.providers.authModule
import com.leochat.features.chat.presentation.providers.chatModule
import com.leochat.features.splash.presentation.providers.splashModule
import com.leochat.features.users.presentation.providers.usersModule
import com.leochat.features.admin.presentation.providers.adminModule
import com.leochat.features.ai.presentation.providers.aiModule
import com.leochat.features.calls.presentation.providers.callsModule
import com.leochat.features.calls.presentation.providers.callsPlatformModule
import com.leochat.features.chat.presentation.providers.contactsModule
import com.leochat.features.media.presentation.providers.mediaModule
import com.leochat.features.media.presentation.providers.mediaPlatformModule
import com.leochat.features.settings.presentation.providers.settingsModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Every Koin module in the app, core first, then features. */
val appModules = listOf(
    platformModule,
    coreModule,
    authModule,
    splashModule,
    chatModule,
    usersModule,
    contactsModule,
    aiModule,
    settingsModule,
    adminModule,
    mediaModule,
    mediaPlatformModule,
    callsModule,
    callsPlatformModule,
)

/** Starts DI once per process. Android passes `androidContext`; iOS passes nothing. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModules)
    }
}
