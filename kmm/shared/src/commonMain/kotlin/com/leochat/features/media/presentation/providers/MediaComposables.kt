package com.leochat.features.media.presentation.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.leochat.features.media.data.datasources.SilentVoicePlayerFactory
import com.leochat.features.media.data.datasources.UnavailableVoiceRecorder
import com.leochat.features.media.domain.repositories.VoicePlayerFactory
import com.leochat.features.media.domain.repositories.VoiceRecorder
import com.leochat.features.media.presentation.controllers.VoiceRecordingController
import org.koin.compose.getKoin

/** Platform player factory from Koin, or a silent fallback when media bindings are not registered. */
@Composable
fun rememberVoicePlayerFactory(): VoicePlayerFactory {
    val koin = getKoin()
    return remember(koin) { koin.getOrNull<VoicePlayerFactory>() ?: SilentVoicePlayerFactory }
}

/** Composer recording controller backed by the platform recorder from Koin. */
@Composable
fun rememberVoiceRecordingController(): VoiceRecordingController {
    val koin = getKoin()
    val scope = rememberCoroutineScope()
    return remember(koin, scope) { VoiceRecordingController(koin.getOrNull<VoiceRecorder>() ?: UnavailableVoiceRecorder, scope) }
}
