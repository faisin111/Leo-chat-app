package com.leochat.feature.calls.di

import com.leochat.feature.calls.data.device.AndroidCallMediaEngineFactory
import com.leochat.feature.calls.data.device.NoOpCallMediaEngineFactory
import com.leochat.feature.calls.domain.model.CallIceConfig
import com.leochat.feature.calls.domain.repository.CallMediaEngine
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val callsPlatformModule: Module = module {
    single<CallMediaEngineFactory> {
        val context = androidContext()
        CallMediaEngineFactory { config ->
            runCatching {
                AndroidCallMediaEngineFactory(context).create(config)
            }.getOrElse {
                NoOpCallMediaEngineFactory.create(config)
            }
        }
    }
}
