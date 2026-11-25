package com.leochat.feature.media.data.repository

import com.leochat.core.common.constants.MediaStrings
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.core.network.safeApiCall
import com.leochat.feature.media.data.remote.MediaFileReader
import com.leochat.feature.media.data.remote.MediaRemoteDataSource
import com.leochat.feature.media.domain.model.MaxUploadBytes
import com.leochat.feature.media.domain.model.UploadedAttachment
import com.leochat.feature.media.domain.model.mimeTypeForVoice
import com.leochat.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

private const val DownloadUrlCacheTtlMs = 10L * 60 * 1000

/** Mirrors `MediaRemoteDataSource` + `mediaApi` + `useMediaDownloadUrl` 10-minute URL cache. */
class MediaRepositoryImpl(
    private val remote: MediaRemoteDataSource,
    private val files: MediaFileReader,
) : MediaRepository {
    private val cacheMutex = Mutex()
    private val urlCache = mutableMapOf<String, Pair<String, Long>>() // storageKey -> (url to expiresAtMs)

    override suspend fun uploadVoice(filePath: String): AppResult<String> {
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) return Either.Right(filePath)
        val mimeType = mimeTypeForVoice(filePath)
        return presignUploadAndConfirm(filePath, mimeType).map { it.url }
    }

    override suspend fun uploadAttachment(
        uriOrPath: String,
        fileName: String,
        mimeType: String,
    ): AppResult<UploadedAttachment> =
        presignUploadAndConfirm(uriOrPath, mimeType).map { uploaded ->
            uploaded.copy(fileName = fileName)
        }

    override suspend fun getDownloadUrl(storageKey: String): AppResult<String> {
        val key = storageKey.trim()
        if (key.isEmpty()) return Either.Left(AppFailure.Validation(MediaStrings.downloadMissingUrl))
        val now = Clock.System.now().toEpochMilliseconds()
        cacheMutex.withLock {
            urlCache[key]?.let { (cachedUrl, expiresAt) ->
                if (now < expiresAt) return Either.Right(cachedUrl)
            }
        }
        return when (val resp = safeApiCall { remote.download(key) }) {
            is Either.Left -> resp
            is Either.Right -> {
                val url = resp.value.url?.takeIf { it.isNotBlank() }
                    ?: return Either.Left(AppFailure.Unknown(MediaStrings.downloadMissingUrl))
                val ttlMs = if (resp.value.expiresIn > 60) (resp.value.expiresIn - 60) * 1000L else DownloadUrlCacheTtlMs
                cacheMutex.withLock {
                    urlCache[key] = url to (now + ttlMs.coerceAtMost(DownloadUrlCacheTtlMs))
                }
                Either.Right(url)
            }
        }
    }

    private suspend fun presignUploadAndConfirm(
        uriOrPath: String,
        mimeType: String,
    ): AppResult<UploadedAttachment> {
        val bytes = runCatching { files.read(uriOrPath) }.getOrNull()
            ?: return Either.Left(AppFailure.Validation(MediaStrings.fileNotFound))
        if (bytes.size > MaxUploadBytes) return Either.Left(AppFailure.Validation(MediaStrings.fileTooLarge))

        val target = when (val presign = safeApiCall { remote.presign(mimeType, bytes.size.toLong()) }) {
            is Either.Left -> return presign
            is Either.Right -> presign.value.toDomain(mimeType)
                ?: return Either.Left(AppFailure.Unknown(MediaStrings.presignIncomplete))
        }
        val uploaded = safeApiCall { remote.upload(target.uploadUrl, bytes, target.contentType) }
        if (uploaded is Either.Left) return uploaded

        return when (val confirm = safeApiCall { remote.confirm(target.storageKey) }) {
            is Either.Left -> confirm
            is Either.Right -> {
                val url = confirm.value.url?.takeIf { it.isNotBlank() }
                    ?: return Either.Left(AppFailure.Unknown(MediaStrings.confirmMissingUrl))
                val confirmedKey = confirm.value.storageKey?.takeIf { it.isNotBlank() } ?: target.storageKey
                val now = Clock.System.now().toEpochMilliseconds()
                cacheMutex.withLock {
                    urlCache[confirmedKey] = url to (now + DownloadUrlCacheTtlMs)
                }
                Either.Right(
                    UploadedAttachment(
                        storageKey = confirmedKey,
                        url = url,
                        fileName = "",
                        mimeType = confirm.value.contentType?.takeIf { it.isNotBlank() } ?: target.contentType,
                        sizeBytes = bytes.size.toLong(),
                    ),
                )
            }
        }
    }
}
