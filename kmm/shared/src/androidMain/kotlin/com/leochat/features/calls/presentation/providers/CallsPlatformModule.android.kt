package com.leochat.features.calls.presentation.providers

import com.leochat.features.calls.data.datasources.AndroidCallMediaEngineFactory
import com.leochat.features.calls.domain.repositories.CallMediaEngineFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val callsPlatformModule: Module = module {
    single<CallMediaEngineFactory> { AndroidCallMediaEngineFactory(androidContext()) }
}
