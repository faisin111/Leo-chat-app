package com.leochat.feature.media.data.device

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import com.leochat.feature.media.domain.model.VoicePlaybackState
import com.leochat.feature.media.domain.repository.VoicePlayer
import com.leochat.feature.media.domain.repository.VoicePlayerFactory
import com.leochat.feature.media.domain.repository.VoiceRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/** AAC/MPEG-4 microphone recorder (44.1 kHz mono, like Flutter's `record` setup). */
class AndroidVoiceRecorder(private val context: Context) : VoiceRecorder {
    private var recorder: MediaRecorder? = null
    private var output: File? = null

    override fun start(): Boolean {
        if (recorder != null) return true
        val file = File(context.cacheDir, "leo_voice_${System.currentTimeMillis()}.m4a")
        return runCatching {
            val created = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
            created.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = created
            output = file
            true
        }.getOrElse {
            file.delete()
            false
        }
    }

    override fun stop(): String? {
        val active = recorder ?: return null
        val stopped = runCatching { active.stop() }.isSuccess
        active.release()
        recorder = null
        val file = output
        output = null
        return file?.takeIf { stopped && it.exists() && it.length() > 0 }?.absolutePath ?: run {
            file?.delete()
            null
        }
    }

    override fun cancel() {
        recorder?.let { active ->
            runCatching { active.stop() }
            active.release()
        }
        recorder = null
        output?.delete()
        output = null
    }
}

/** [MediaPlayer]-backed voice note player; position is polled every 50 ms while playing. */
class AndroidVoicePlayer : VoicePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mutableState = MutableStateFlow(VoicePlaybackState())
    override val state: StateFlow<VoicePlaybackState> = mutableState.asStateFlow()

    private var player: MediaPlayer? = null
    private var source: String? = null
    private var prepared = false
    private var speed = 1f
    private var pendingSeek: Float? = null
    private var ticker: Job? = null

    override fun play(source: String) {
        val current = player
        if (current != null && prepared && this.source == source) {
            start(current)
            return
        }
        releasePlayer()
        this.source = source
        mutableState.update { it.copy(isLoading = true, hasError = false) }
        val created = MediaPlayer()
        player = created
        runCatching {
            created.setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
            )
            created.setDataSource(source)
            created.setOnPreparedListener { mp ->
                prepared = true
                mutableState.update { it.copy(isLoading = false, durationMs = mp.duration.toLong().coerceAtLeast(0)) }
                pendingSeek?.let { fraction -> mp.seekTo((mp.duration * fraction).toInt()) }
                pendingSeek = null
                start(mp)
            }
            created.setOnCompletionListener { mp ->
                ticker?.cancel()
                mp.seekTo(0)
                mutableState.update { it.copy(isPlaying = false, positionMs = 0) }
            }
            created.setOnErrorListener { _, _, _ ->
                releasePlayer()
                mutableState.update { it.copy(isPlaying = false, isLoading = false, hasError = true) }
                true
            }
            created.prepareAsync()
        }.onFailure {
            releasePlayer()
            mutableState.update { it.copy(isPlaying = false, isLoading = false, hasError = true) }
        }
    }

    override fun pause() {
        player?.takeIf { prepared && it.isPlaying }?.pause()
        ticker?.cancel()
        mutableState.update { it.copy(isPlaying = false) }
    }

    override fun seekTo(fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        val current = player
        if (current != null && prepared) {
            val target = (current.duration * clamped).toInt()
            current.seekTo(target)
            mutableState.update { it.copy(positionMs = target.toLong()) }
        } else {
            pendingSeek = clamped
            mutableState.update { it.copy(positionMs = (it.durationMs * clamped).toLong()) }
        }
    }

    override fun setSpeed(speed: Float) {
        this.speed = speed
        val current = player ?: return
        if (prepared && current.isPlaying) applySpeed(current)
    }

    override fun release() {
        releasePlayer()
        scope.cancel()
    }

    private fun start(mp: MediaPlayer) {
        runCatching {
            mp.start()
            applySpeed(mp)
        }
        mutableState.update { it.copy(isPlaying = true) }
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val position = runCatching { mp.currentPosition.toLong() }.getOrDefault(0)
                mutableState.update { it.copy(positionMs = position) }
                delay(50)
            }
        }
    }

    private fun applySpeed(mp: MediaPlayer) {
        runCatching { mp.playbackParams = mp.playbackParams.setSpeed(speed) }
    }

    private fun releasePlayer() {
        ticker?.cancel()
        player?.let { runCatching { it.release() } }
        player = null
        prepared = false
    }
}

val AndroidVoicePlayerFactory = VoicePlayerFactory { AndroidVoicePlayer() }
