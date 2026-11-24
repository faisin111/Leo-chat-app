package com.leochat.features.calls.presentation.providers

import com.leochat.core.di.ApplicationScope
import com.leochat.features.calls.data.datasources.CallSignalingDataSource
import com.leochat.features.calls.data.datasources.NoOpCallMediaEngineFactory
import com.leochat.features.calls.data.datasources.StompCallSignalingDataSource
import com.leochat.features.calls.data.repositories.CallSignalingRepositoryImpl
import com.leochat.features.calls.domain.entities.CallIceConfig
import com.leochat.features.calls.domain.repositories.CallMediaEngineFactory
import com.leochat.features.calls.domain.repositories.CallSignalingRepository
import com.leochat.features.calls.domain.usecases.ObserveCallSignalsUseCase
import com.leochat.features.calls.domain.usecases.SendCallSignalUseCase
import com.leochat.features.calls.presentation.controllers.CallController
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Calls graph. [CallController] is a `single` so the app root (incoming call
 * banner) and the active call page share one call. The WebRTC engine comes from
 * [callsPlatformModule]; without it calls fall back to signaling only.
 */
val callsModule = module {
    single<CallSignalingDataSource> { StompCallSignalingDataSource(get()) }
    single<CallSignalingRepository> { CallSignalingRepositoryImpl(get()) }
    factoryOf(::ObserveCallSignalsUseCase)
    factoryOf(::SendCallSignalUseCase)
    single { CallIceConfig.Default }
    single {
        CallController(
            observeSignals = get(),
            sendSignal = get(),
            mediaFactory = getOrNull<CallMediaEngineFactory>() ?: NoOpCallMediaEngineFactory,
            iceConfig = get(),
            scope = get(ApplicationScope),
        )
    }
}

/** Binds [CallMediaEngineFactory]: WebRTC on Android, a signaling-only stub on iOS. */
expect val callsPlatformModule: Module
