package com.leochat.core.network.di

import com.leochat.core.common.di.ApplicationScope
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.network.config.AppConfig
import com.leochat.core.network.createHttpClient
import com.leochat.core.network.push.NoOpPushTokenProvider
import com.leochat.core.network.push.PushTokenProvider
import com.leochat.core.network.push.PushTokenRegistry
import com.leochat.core.network.stomp.StompClient
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/** One configured Ktor client, STOMP client, and push token registry for the whole app. */
val networkModule = module {
    single { AppConfig.Production }
    single { TokenStore(get()) }
    single { SessionEvents() }
    single<HttpClient> { createHttpClient(engine = get(), config = get(), tokens = get(), sessionEvents = get()) }
    single { StompClient(get(), get(), get(), get(ApplicationScope)) }
    single { PushTokenRegistry(get(), get(), get(ApplicationScope), getOrNull<PushTokenProvider>() ?: NoOpPushTokenProvider) }
}

/** Binds the platform `HttpClientEngine` (OkHttp on Android, Darwin on iOS). */
expect val httpEngineModule: Module
