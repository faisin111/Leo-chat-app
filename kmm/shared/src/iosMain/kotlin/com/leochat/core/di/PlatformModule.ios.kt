package com.leochat.core.di

import com.leochat.core.storage.IosKeyValueStorage
import com.leochat.core.storage.IosSecureStorage
import com.leochat.core.storage.KeyValueStorage
import com.leochat.core.storage.SecureStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SecureStorage> { IosSecureStorage() }
    single<KeyValueStorage> { IosKeyValueStorage() }
    single<HttpClientEngine> { Darwin.create() }
}
