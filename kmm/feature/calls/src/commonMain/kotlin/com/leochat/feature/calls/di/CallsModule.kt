package com.leochat.feature.calls.di

import com.leochat.core.common.di.ApplicationScope
import com.leochat.feature.calls.data.remote.CallSignalingDataSource
import com.leochat.feature.calls.data.device.NoOpCallMediaEngineFactory
import com.leochat.feature.calls.data.remote.StompCallSignalingDataSource
import com.leochat.feature.calls.data.repository.CallSignalingRepositoryImpl
import com.leochat.feature.calls.domain.model.CallIceConfig
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import com.leochat.feature.calls.domain.repository.CallSignalingRepository
import com.leochat.feature.calls.domain.repository.CallSoundPlayer
import com.leochat.feature.calls.domain.repository.NoOpCallSoundPlayer
import com.leochat.feature.calls.domain.usecase.ObserveCallSignalsUseCase
import com.leochat.feature.calls.domain.usecase.SendCallSignalUseCase
import com.leochat.feature.calls.presentation.CallController
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
            soundPlayer = getOrNull<CallSoundPlayer>() ?: NoOpCallSoundPlayer,
        )
    }
}

/** Binds [CallMediaEngineFactory]: WebRTC on Android, a signaling-only stub on iOS. */
expect val callsPlatformModule: Module
