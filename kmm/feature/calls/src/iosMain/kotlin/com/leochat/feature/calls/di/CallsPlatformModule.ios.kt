package com.leochat.feature.calls.di

import com.leochat.feature.calls.data.device.NoOpCallMediaEngineFactory
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS stub: signaling-only calls until a WebRTC framework is linked. */
actual val callsPlatformModule: Module = module {
    single<CallMediaEngineFactory> { NoOpCallMediaEngineFactory }
}
