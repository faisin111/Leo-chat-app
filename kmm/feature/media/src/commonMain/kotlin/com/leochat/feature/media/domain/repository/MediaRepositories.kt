package com.leochat.feature.media.domain.repository

import com.leochat.core.common.result.AppResult
import com.leochat.feature.media.domain.model.UploadedAttachment
import com.leochat.feature.media.domain.model.VoicePlaybackState
import kotlinx.coroutines.flow.StateFlow

interface MediaRepository {
    /** presign → PUT bytes (no Bearer) → confirm. Returns the public media URL. */
    suspend fun uploadVoice(filePath: String): AppResult<String>

    /** presign → PUT bytes (no Bearer) → confirm for any image or document up to 10 MB. */
    suspend fun uploadAttachment(
        uriOrPath: String,
        fileName: String,
        mimeType: String,
    ): AppResult<UploadedAttachment>

    /** Resolves and caches an authorized download URL for an existing `storageKey`. */
    suspend fun getDownloadUrl(storageKey: String): AppResult<String>
}

/** Platform microphone recorder. One recording at a time. */
interface VoiceRecorder {
    /** Starts recording to a new temp file. Returns false when the platform cannot record. */
    fun start(): Boolean

    /** Stops and returns the file path, or null when nothing was recorded. */
    fun stop(): String?

    /** Stops and deletes the current file. */
    fun cancel()
}

/** Platform audio player for one voice note. */
interface VoicePlayer {
    val state: StateFlow<VoicePlaybackState>
    fun play(source: String)
    fun pause()
    fun seekTo(fraction: Float)
    fun setSpeed(speed: Float)
    fun release()
}

fun interface VoicePlayerFactory {
    fun create(): VoicePlayer
}
