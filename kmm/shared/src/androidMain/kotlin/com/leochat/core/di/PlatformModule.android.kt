package com.leochat.core.di

import com.leochat.core.storage.AndroidKeyValueStorage
import com.leochat.core.storage.AndroidSecureStorage
import com.leochat.core.storage.KeyValueStorage
import com.leochat.core.storage.SecureStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SecureStorage> { AndroidSecureStorage(androidContext()) }
    single<KeyValueStorage> { AndroidKeyValueStorage(androidContext()) }
    single<HttpClientEngine> { OkHttp.create() }
}
