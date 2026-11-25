package com.leochat.feature.calls.data.device

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import com.leochat.feature.calls.domain.repository.CallSoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android ringback, incoming ringtone, and call-end tone player.
 * All audio calls are guarded by [runCatching] so missing audio hardware never crashes calls.
 */
class AndroidCallSoundPlayer(private val context: Context) : CallSoundPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Any()
    private var ringJob: Job? = null
    private var ringtone: Ringtone? = null
    private var toneGenerator: ToneGenerator? = null

    override fun startOutgoingRing() {
        stopRing()
        synchronized(lock) {
            ringJob = scope.launch {
                val gen = runCatching { ToneGenerator(AudioManager.STREAM_VOICE_CALL, 55) }.getOrNull()
                synchronized(lock) { toneGenerator = gen }
                while (isActive) {
                    runCatching { gen?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 1_500) }
                    delay(4_000L)
                }
            }
        }
    }

    override fun startIncomingRing() {
        stopRing()
        synchronized(lock) {
            val uri = runCatching { RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) }.getOrNull()
            val ring = uri?.let { runCatching { RingtoneManager.getRingtone(context.applicationContext, it) }.getOrNull() }
            if (ring != null) {
                runCatching {
                    ring.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ring.isLooping = true
                    }
                    ring.play()
                }
                ringtone = ring
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    ringJob = scope.launch {
                        while (isActive) {
                            delay(2_600L)
                            runCatching { if (!ring.isPlaying) ring.play() }
                        }
                    }
                }
            } else {
                ringJob = scope.launch {
                    val gen = runCatching { ToneGenerator(AudioManager.STREAM_RING, 70) }.getOrNull()
                    synchronized(lock) { toneGenerator = gen }
                    while (isActive) {
                        runCatching { gen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 600) }
                        delay(2_600L)
                    }
                }
            }
        }
    }

    override fun stopRing() {
        synchronized(lock) {
            ringJob?.cancel()
            ringJob = null
            runCatching { ringtone?.stop() }
            ringtone = null
            runCatching {
                toneGenerator?.stopTone()
                toneGenerator?.release()
            }
            toneGenerator = null
        }
    }

    override fun playCallEnd() {
        stopRing()
        scope.launch {
            val gen = runCatching { ToneGenerator(AudioManager.STREAM_VOICE_CALL, 55) }.getOrNull() ?: return@launch
            runCatching { gen.startTone(ToneGenerator.TONE_PROP_NACK, 240) }
            delay(320L)
            runCatching { gen.release() }
        }
    }
}
