package com.leochat.features.media.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.MediaStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.features.media.presentation.providers.rememberVoicePlayerFactory
import com.leochat.features.media.presentation.states.VoicePlaybackSpeeds
import com.leochat.features.media.presentation.states.formatVoiceTime
import kotlin.math.sin

// Waveform profile from `_waveformHeights`.
private val WaveformHeights = listOf(7, 13, 20, 12, 26, 32, 18, 24, 30, 14, 22, 28, 34, 18, 12, 22, 28, 16, 10, 20, 26, 14, 8, 18, 24, 12, 8)
private val InactiveWaveIncoming = Color(0xFFE2D9D2)
private val OwnPlayShadow = Color(0x24000000)
private val IncomingPlayShadow = Color(0x33E64A19)

/**
 * Voice note player with play/pause, seekable waveform, elapsed/total time, and
 * 1x/1.5x/2x speed. Mirrors `LeoVoiceMessagePlayer`, with real platform playback.
 */
@Composable
fun VoiceMessagePlayer(isOwn: Boolean, audioUrl: String?, modifier: Modifier = Modifier) {
    val factory = rememberVoicePlayerFactory()
    val player = remember(audioUrl) { factory.create() }
    DisposableEffect(player) { onDispose { player.release() } }
    val playback by player.state.collectAsStateWithLifecycle()
    var speed by remember { mutableFloatStateOf(1f) }

    val progress = playback.progress
    val showElapsed = playback.isPlaying || playback.positionMs > 0
    val displayTime = formatVoiceTime(if (showElapsed) playback.positionMs else playback.durationMs)
    val activeWave = if (isOwn) Color.White else LeoColors.primary
    val inactiveWave = if (isOwn) Color.White.copy(alpha = 0.35f) else InactiveWaveIncoming

    Column(modifier.widthIn(min = 240.dp, max = 320.dp).padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayButton(
                isOwn = isOwn,
                isPlaying = playback.isPlaying,
                isLoading = playback.isLoading,
                onClick = {
                    if (playback.isPlaying) player.pause() else audioUrl?.takeIf { it.isNotBlank() }?.let(player::play)
                },
            )
            Spacer(Modifier.width(12.dp))
            Waveform(
                progress = progress,
                isPlaying = playback.isPlaying,
                active = activeWave,
                inactive = inactiveWave,
                onSeek = player::seekTo,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(start = 54.dp, end = 4.dp),
        ) {
            Text(
                displayTime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp,
                color = if (isOwn) Color.White.copy(alpha = 0.9f) else LeoColors.textSecondary,
            )
            SpeedChip(isOwn, speed) {
                speed = VoicePlaybackSpeeds.next(speed)
                player.setSpeed(speed)
            }
        }
    }
}

@Composable
private fun PlayButton(isOwn: Boolean, isPlaying: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val glow = if (isOwn) OwnPlayShadow else IncomingPlayShadow
    val tint = if (isOwn) LeoColors.primary else Color.White
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .leoPressable(interaction)
            .size(42.dp)
            .shadow(8.dp, CircleShape, ambientColor = glow, spotColor = glow)
            .clip(CircleShape)
            .background(if (isOwn) Color.White else LeoColors.primary)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick),
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = tint, strokeWidth = 2.dp)
        } else {
            Icon(
                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) MediaStrings.pause else MediaStrings.play,
                tint = tint,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun Waveform(
    progress: Float,
    isPlaying: Boolean,
    active: Color,
    inactive: Color,
    onSeek: (Float) -> Unit,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(38.dp)
            .pointerInput(Unit) { detectTapGestures { onSeek(it.x / size.width) } }
            .pointerInput(Unit) { detectHorizontalDragGestures { change, _ -> onSeek(change.position.x / size.width) } },
    ) {
        val last = WaveformHeights.lastIndex
        WaveformHeights.forEachIndexed { i, base ->
            val passed = progress >= i.toFloat() / last
            val height = if (isPlaying) {
                (base + sin(progress * 15 + i * 0.45f) * 3.5f).coerceIn(4f, 36f)
            } else {
                base.toFloat()
            }
            Box(Modifier.width(3.2.dp).height(height.dp).background(if (passed) active else inactive, RoundedCornerShape(3.dp)))
        }
    }
}

@Composable
private fun SpeedChip(isOwn: Boolean, speed: Float, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Text(
        VoicePlaybackSpeeds.label(speed),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (isOwn) Color.White else LeoColors.primary,
        modifier = Modifier
            .leoPressable(interaction)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isOwn) Color.White.copy(alpha = 0.2f) else LeoColors.input)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}
