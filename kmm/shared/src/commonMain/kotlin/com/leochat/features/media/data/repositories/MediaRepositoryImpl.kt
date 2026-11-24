package com.leochat.features.media.data.repositories

import com.leochat.core.constants.MediaStrings
import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.core.network.safeApiCall
import com.leochat.features.media.data.datasources.MediaFileReader
import com.leochat.features.media.data.datasources.MediaRemoteDataSource
import com.leochat.features.media.domain.entities.MaxUploadBytes
import com.leochat.features.media.domain.entities.mimeTypeForVoice
import com.leochat.features.media.domain.repositories.MediaRepository

/** Mirrors `MediaRemoteDataSource.uploadVoice` + `MediaRepositoryImpl` in Flutter. */
class MediaRepositoryImpl(
    private val remote: MediaRemoteDataSource,
    private val files: MediaFileReader,
) : MediaRepository {
    override suspend fun uploadVoice(filePath: String): AppResult<String> {
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) return Either.Right(filePath)
        val bytes = runCatching { files.read(filePath) }.getOrNull()
            ?: return Either.Left(AppFailure.Validation(MediaStrings.fileNotFound))
        if (bytes.size > MaxUploadBytes) return Either.Left(AppFailure.Validation(MediaStrings.fileTooLarge))
        val mimeType = mimeTypeForVoice(filePath)

        val target = when (val presign = safeApiCall { remote.presign(mimeType, bytes.size.toLong()) }) {
            is Either.Left -> return presign
            is Either.Right -> presign.value.toDomain(mimeType)
                ?: return Either.Left(AppFailure.Unknown(MediaStrings.presignIncomplete))
        }
        val uploaded = safeApiCall { remote.upload(target.uploadUrl, bytes, target.contentType) }
        if (uploaded is Either.Left) return uploaded

        return when (val confirm = safeApiCall { remote.confirm(target.storageKey) }) {
            is Either.Left -> confirm
            is Either.Right -> confirm.value.url?.takeIf { it.isNotBlank() }?.let { Either.Right(it) }
                ?: Either.Left(AppFailure.Unknown(MediaStrings.confirmMissingUrl))
        }
    }
}
