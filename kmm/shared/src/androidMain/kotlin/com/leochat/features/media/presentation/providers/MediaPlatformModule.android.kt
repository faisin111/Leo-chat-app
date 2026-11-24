package com.leochat.features.media.presentation.providers

import com.leochat.features.media.data.datasources.AndroidVoicePlayerFactory
import com.leochat.features.media.data.datasources.AndroidVoiceRecorder
import com.leochat.features.media.domain.repositories.VoicePlayerFactory
import com.leochat.features.media.domain.repositories.VoiceRecorder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val mediaPlatformModule: Module = module {
    factory<VoiceRecorder> { AndroidVoiceRecorder(androidContext()) }
    single<VoicePlayerFactory> { AndroidVoicePlayerFactory }
}
