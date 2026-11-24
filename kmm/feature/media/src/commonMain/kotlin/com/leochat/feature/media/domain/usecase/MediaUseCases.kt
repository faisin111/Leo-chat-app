package com.leochat.feature.media.domain.usecase

import com.leochat.core.common.constants.MediaStrings
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.failure
import com.leochat.feature.media.domain.model.VoiceMessageDraft
import com.leochat.feature.media.domain.model.VoiceRecording
import com.leochat.feature.media.domain.repository.MediaRepository

class UploadVoiceUseCase(private val repository: MediaRepository) {
    suspend operator fun invoke(filePath: String): AppResult<String> {
        if (filePath.isBlank()) return failure(AppFailure.Validation(MediaStrings.fileNotFound))
        return repository.uploadVoice(filePath.trim())
    }
}

/**
 * Uploads a recording and returns what chat sends: `content` = media URL,
 * `type` = `VOICE`. Already-uploaded URLs (retries) pass through unchanged.
 */
class SendVoiceMessageUseCase(private val upload: UploadVoiceUseCase) {
    suspend operator fun invoke(recording: VoiceRecording): AppResult<VoiceMessageDraft> =
        upload(recording.filePath).map { url -> VoiceMessageDraft(content = url, durationSeconds = recording.durationSeconds) }

    /** Retry path: Flutter retries a failed voice message from its stored content (local path or URL). */
    suspend operator fun invoke(filePathOrUrl: String): AppResult<VoiceMessageDraft> =
        upload(filePathOrUrl).map { url -> VoiceMessageDraft(content = url) }
}
