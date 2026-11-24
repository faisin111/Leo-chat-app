package com.leochat.feature.media.di

import com.leochat.feature.media.data.device.SilentVoicePlayerFactory
import com.leochat.feature.media.data.device.UnavailableVoiceRecorder
import com.leochat.feature.media.domain.repository.VoicePlayerFactory
import com.leochat.feature.media.domain.repository.VoiceRecorder
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS stub bindings: recording is unavailable and playback only tracks UI state (not built on Windows). */
actual val mediaPlatformModule: Module = module {
    single<VoiceRecorder> { UnavailableVoiceRecorder }
    single<VoicePlayerFactory> { SilentVoicePlayerFactory }
}
