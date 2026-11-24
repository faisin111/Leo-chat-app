package com.leochat.features.media.domain.usecases

import com.leochat.core.constants.MediaStrings
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.failure
import com.leochat.features.media.domain.entities.VoiceMessageDraft
import com.leochat.features.media.domain.entities.VoiceRecording
import com.leochat.features.media.domain.repositories.MediaRepository

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
