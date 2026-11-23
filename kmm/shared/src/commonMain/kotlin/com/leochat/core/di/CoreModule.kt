package com.leochat.core.di

import com.leochat.core.cache.CacheStore
import com.leochat.core.cache.KeyValueCacheStore
import com.leochat.core.config.AppConfig
import com.leochat.core.network.createHttpClient
import com.leochat.core.network.stomp.StompClient
import com.leochat.core.session.SessionEvents
import com.leochat.core.session.TokenStore
import com.leochat.core.theme.ThemeModeStore
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val ApplicationScope = named("applicationScope")

/**
 * Shared infrastructure. Platform storage and the HTTP engine come from
 * [platformModule]; feature modules depend only on these bindings.
 */
val coreModule = module {
    single { AppConfig.Production }
    single<CoroutineScope>(ApplicationScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { TokenStore(get()) }
    single { SessionEvents() }
    single<HttpClient> { createHttpClient(engine = get(), config = get(), tokens = get(), sessionEvents = get()) }
    single { StompClient(get(), get(), get(), get(ApplicationScope)) }
    single<CacheStore> { KeyValueCacheStore(get()) }
    single { ThemeModeStore(get()) }
}

/** Provides SecureStorage, KeyValueStorage, and HttpClientEngine per platform. */
expect val platformModule: Module
