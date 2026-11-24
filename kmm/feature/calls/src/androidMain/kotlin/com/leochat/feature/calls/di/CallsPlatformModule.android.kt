package com.leochat.feature.calls.di

import com.leochat.feature.calls.data.device.AndroidCallMediaEngineFactory
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val callsPlatformModule: Module = module {
    single<CallMediaEngineFactory> { AndroidCallMediaEngineFactory(androidContext()) }
}
