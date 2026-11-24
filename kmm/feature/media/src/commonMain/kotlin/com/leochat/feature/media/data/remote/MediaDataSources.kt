package com.leochat.feature.media.data.remote

import com.leochat.feature.media.data.remote.ConfirmRequestDto
import com.leochat.feature.media.data.remote.ConfirmResponseDto
import com.leochat.feature.media.data.remote.PresignRequestDto
import com.leochat.feature.media.data.remote.PresignResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/**
 * Media endpoints. [apiClient] is the shared authenticated client; [uploadClient]
 * is a bare client so presigned storage PUTs never carry the Bearer token
 * (Flutter uses a fresh `Dio()` for the same reason).
 */
interface MediaRemoteDataSource {
    suspend fun presign(mimeType: String, sizeInBytes: Long): PresignResponseDto
    suspend fun upload(uploadUrl: String, bytes: ByteArray, contentType: String)
    suspend fun confirm(storageKey: String): ConfirmResponseDto
}

class MediaRemoteDataSourceImpl(
    private val apiClient: HttpClient,
    private val uploadClient: HttpClient,
) : MediaRemoteDataSource {
    override suspend fun presign(mimeType: String, sizeInBytes: Long): PresignResponseDto =
        apiClient.post("media/presign") {
            contentType(ContentType.Application.Json)
            setBody(PresignRequestDto(mimeType, sizeInBytes))
        }.body()

    override suspend fun upload(uploadUrl: String, bytes: ByteArray, contentType: String) {
        uploadClient.put(uploadUrl) { setBody(ByteArrayContent(bytes, ContentType.parse(contentType))) }
    }

    override suspend fun confirm(storageKey: String): ConfirmResponseDto =
        apiClient.post("media/confirm") {
            contentType(ContentType.Application.Json)
            setBody(ConfirmRequestDto(storageKey))
        }.body()
}

/** Reads local recordings. Returns null when the file does not exist. */
fun interface MediaFileReader {
    fun read(path: String): ByteArray?
}

class SystemMediaFileReader : MediaFileReader {
    override fun read(path: String): ByteArray? {
        val file = Path(path)
        if (!SystemFileSystem.exists(file)) return null
        return SystemFileSystem.source(file).buffered().use { it.readByteArray() }
    }
}
