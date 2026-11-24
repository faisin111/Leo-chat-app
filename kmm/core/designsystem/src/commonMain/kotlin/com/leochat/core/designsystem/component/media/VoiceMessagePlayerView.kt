package com.leochat.core.designsystem.component.media

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.MediaStrings
import com.leochat.core.designsystem.component.buttons.leoPressable
import com.leochat.core.designsystem.component.media.model.VoicePlaybackUi
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.MediaTokens
import kotlin.math.sin

/**
 * Stateless voice note player: play/pause, seekable waveform, time label, and speed chip.
 * Mirrors `LeoVoiceMessagePlayer`; the caller owns playback and passes a [VoicePlaybackUi] snapshot.
 */
@Composable
fun VoiceMessagePlayerView(
    isOwn: Boolean,
    playback: VoicePlaybackUi,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onSpeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeWave = if (isOwn) Color.White else LeoColors.primary
    val inactiveWave = if (isOwn) Color.White.copy(alpha = 0.35f) else MediaTokens.InactiveWaveIncoming

    Column(modifier.widthIn(min = 240.dp, max = 320.dp).padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VoicePlayButton(
                isOwn = isOwn,
                isPlaying = playback.isPlaying,
                isLoading = playback.isLoading,
                onClick = onTogglePlay,
            )
            Spacer(Modifier.width(12.dp))
            VoiceWaveform(
                progress = playback.progress,
                isPlaying = playback.isPlaying,
                active = activeWave,
                inactive = inactiveWave,
                onSeek = onSeek,
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
                playback.timeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp,
                color = if (isOwn) Color.White.copy(alpha = 0.9f) else LeoColors.textSecondary,
            )
            VoiceSpeedChip(isOwn, playback.speedLabel, onSpeed)
        }
    }
}

/** Round play/pause button with a spinner while the source buffers. */
@Composable
fun VoicePlayButton(isOwn: Boolean, isPlaying: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val glow = if (isOwn) MediaTokens.OwnPlayShadow else MediaTokens.IncomingPlayShadow
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

/** Seekable bar waveform; bars before [progress] use [active], the rest [inactive]. */
@Composable
fun VoiceWaveform(
    progress: Float,
    isPlaying: Boolean,
    active: Color,
    inactive: Color,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(38.dp)
            .pointerInput(Unit) { detectTapGestures { onSeek(it.x / size.width) } }
            .pointerInput(Unit) { detectHorizontalDragGestures { change, _ -> onSeek(change.position.x / size.width) } },
    ) {
        val heights = MediaTokens.WaveformHeights
        val last = heights.lastIndex
        heights.forEachIndexed { i, base ->
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

/** Small pill showing the current playback speed label. */
@Composable
fun VoiceSpeedChip(isOwn: Boolean, label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Text(
        label,
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
