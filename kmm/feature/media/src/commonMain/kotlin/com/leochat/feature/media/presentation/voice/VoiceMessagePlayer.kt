package com.leochat.feature.media.presentation.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.media.VoiceMessagePlayerView
import com.leochat.feature.media.presentation.VoicePlaybackSpeeds
import com.leochat.feature.media.presentation.mapper.toUi

/**
 * Voice note player with real platform playback. Owns the player from Koin and
 * renders the stateless [VoiceMessagePlayerView]. Mirrors `LeoVoiceMessagePlayer`.
 */
@Composable
fun VoiceMessagePlayer(isOwn: Boolean, audioUrl: String?, modifier: Modifier = Modifier) {
    val factory = rememberVoicePlayerFactory()
    val player = remember(audioUrl) { factory.create() }
    DisposableEffect(player) { onDispose { player.release() } }
    val playback by player.state.collectAsStateWithLifecycle()
    var speed by remember { mutableFloatStateOf(1f) }

    VoiceMessagePlayerView(
        isOwn = isOwn,
        playback = playback.toUi(speed),
        onTogglePlay = {
            if (playback.isPlaying) player.pause() else audioUrl?.takeIf { it.isNotBlank() }?.let(player::play)
        },
        onSeek = player::seekTo,
        onSpeed = {
            speed = VoicePlaybackSpeeds.next(speed)
            player.setSpeed(speed)
        },
        modifier = modifier,
    )
}
