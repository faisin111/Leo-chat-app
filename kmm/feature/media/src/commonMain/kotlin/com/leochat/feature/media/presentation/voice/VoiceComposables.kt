package com.leochat.feature.media.presentation.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.leochat.feature.media.data.device.SilentVoicePlayerFactory
import com.leochat.feature.media.data.device.UnavailableVoiceRecorder
import com.leochat.feature.media.domain.repository.VoicePlayerFactory
import com.leochat.feature.media.domain.repository.VoiceRecorder
import com.leochat.feature.media.presentation.VoiceRecordingViewModel
import org.koin.compose.getKoin

/** Platform player factory from Koin, or a silent fallback when media bindings are not registered. */
@Composable
fun rememberVoicePlayerFactory(): VoicePlayerFactory {
    val koin = getKoin()
    return remember(koin) { koin.getOrNull<VoicePlayerFactory>() ?: SilentVoicePlayerFactory }
}

/** Composer recording controller backed by the platform recorder from Koin. */
@Composable
fun rememberVoiceRecordingController(): VoiceRecordingViewModel {
    val koin = getKoin()
    val scope = rememberCoroutineScope()
    return remember(koin, scope) { VoiceRecordingViewModel(koin.getOrNull<VoiceRecorder>() ?: UnavailableVoiceRecorder, scope) }
}
