package com.leochat.feature.media.di

import com.leochat.feature.media.data.device.AndroidMediaFileReader
import com.leochat.feature.media.data.device.AndroidVoicePlayerFactory
import com.leochat.feature.media.data.device.AndroidVoiceRecorder
import com.leochat.feature.media.data.remote.MediaFileReader
import com.leochat.feature.media.domain.repository.VoicePlayerFactory
import com.leochat.feature.media.domain.repository.VoiceRecorder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val mediaPlatformModule: Module = module {
    single<MediaFileReader> { AndroidMediaFileReader(androidContext()) }
    factory<VoiceRecorder> { AndroidVoiceRecorder(androidContext()) }
    single<VoicePlayerFactory> { AndroidVoicePlayerFactory }
}
