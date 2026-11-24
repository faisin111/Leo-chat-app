package com.leochat.features.calls.presentation.providers

import com.leochat.features.calls.data.datasources.NoOpCallMediaEngineFactory
import com.leochat.features.calls.domain.repositories.CallMediaEngineFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS stub: signaling-only calls until a WebRTC framework is linked. */
actual val callsPlatformModule: Module = module {
    single<CallMediaEngineFactory> { NoOpCallMediaEngineFactory }
}
