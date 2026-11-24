package com.leochat.features.media.presentation.providers

import com.leochat.features.media.data.datasources.SilentVoicePlayerFactory
import com.leochat.features.media.data.datasources.UnavailableVoiceRecorder
import com.leochat.features.media.domain.repositories.VoicePlayerFactory
import com.leochat.features.media.domain.repositories.VoiceRecorder
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS stub bindings: recording is unavailable and playback only tracks UI state (not built on Windows). */
actual val mediaPlatformModule: Module = module {
    single<VoiceRecorder> { UnavailableVoiceRecorder }
    single<VoicePlayerFactory> { SilentVoicePlayerFactory }
}
