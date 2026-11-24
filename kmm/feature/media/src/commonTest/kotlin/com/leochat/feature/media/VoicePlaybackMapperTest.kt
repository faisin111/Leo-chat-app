package com.leochat.feature.media

import com.leochat.core.designsystem.component.media.model.VoicePlaybackUi
import com.leochat.feature.media.domain.model.VoicePlaybackState
import com.leochat.feature.media.presentation.mapper.toUi
import kotlin.test.Test
import kotlin.test.assertEquals

class VoicePlaybackMapperTest {
    @Test
    fun idlePlaybackShowsTotalDurationAndSpeedLabel() {
        val ui = VoicePlaybackState(durationMs = 65_000).toUi(speed = 1f)
        assertEquals(VoicePlaybackUi(progress = 0f, timeText = "1:05", speedLabel = "1x"), ui)
    }

    @Test
    fun playingShowsElapsedTimeAndProgress() {
        val ui = VoicePlaybackState(isPlaying = true, positionMs = 15_000, durationMs = 60_000).toUi(speed = 1.5f)
        assertEquals(true, ui.isPlaying)
        assertEquals(0.25f, ui.progress)
        assertEquals("0:15", ui.timeText)
        assertEquals("1.5x", ui.speedLabel)
    }

    @Test
    fun pausedMidWayKeepsElapsedTimeAndLoadingFlag() {
        val ui = VoicePlaybackState(isLoading = true, positionMs = 3_000, durationMs = 10_000).toUi(speed = 2f)
        assertEquals(true, ui.isLoading)
        assertEquals("0:03", ui.timeText)
        assertEquals("2.0x", ui.speedLabel)
    }
}
