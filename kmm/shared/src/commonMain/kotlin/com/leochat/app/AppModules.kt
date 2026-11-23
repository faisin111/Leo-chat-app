package com.leochat.app

import com.leochat.core.di.coreModule
import com.leochat.core.di.platformModule
import com.leochat.features.auth.presentation.providers.authModule
import com.leochat.features.chat.presentation.providers.chatModule
import com.leochat.features.splash.presentation.providers.splashModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Every Koin module in the app, core first, then features. */
val appModules = listOf(
    platformModule,
    coreModule,
    authModule,
    splashModule,
    chatModule,
)

/** Starts DI once per process. Android passes `androidContext`; iOS passes nothing. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModules)
    }
}
