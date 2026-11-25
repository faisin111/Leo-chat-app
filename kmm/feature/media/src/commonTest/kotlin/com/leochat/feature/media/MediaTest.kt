package com.leochat.feature.media

import com.leochat.core.network.config.AppConfig
import com.leochat.core.common.constants.StorageKeys
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.core.network.createHttpClient
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.network.auth.TokenStore
import com.leochat.core.platform.storage.InMemorySecureStorage
import com.leochat.feature.media.data.remote.MediaFileReader
import com.leochat.feature.media.data.remote.MediaRemoteDataSourceImpl
import com.leochat.feature.media.data.repository.MediaRepositoryImpl
import com.leochat.feature.media.domain.model.UploadedAttachment
import com.leochat.feature.media.domain.model.VoiceRecording
import com.leochat.feature.media.domain.model.mimeTypeForVoice
import com.leochat.feature.media.domain.repository.MediaRepository
import com.leochat.feature.media.domain.repository.VoiceRecorder
import com.leochat.feature.media.domain.usecase.GetMediaDownloadUrlUseCase
import com.leochat.feature.media.domain.usecase.SendVoiceMessageUseCase
import com.leochat.feature.media.domain.usecase.UploadAttachmentUseCase
import com.leochat.feature.media.domain.usecase.UploadVoiceUseCase
import com.leochat.feature.media.presentation.VoiceRecordingViewModel
import com.leochat.feature.media.di.createMediaUploadClient
import com.leochat.feature.media.presentation.VoicePlaybackSpeeds
import com.leochat.feature.media.presentation.formatRecordingTime
import com.leochat.feature.media.presentation.formatVoiceTime
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MediaRepositoryImplTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val requests = mutableListOf<HttpRequestData>()

    private fun repository(
        files: MediaFileReader = MediaFileReader { ByteArray(256) { 1 } },
        presignBody: String = """{"uploadUrl":"https://storage.test/bucket/voice.m4a?sig=1","storageKey":"voice/k1","contentType":"audio/mp4"}""",
        confirmBody: String = """{"url":"https://cdn.test/voice/k1.m4a"}""",
        downloadBody: String = """{"storageKey":"img/k2","url":"https://cdn.test/img/k2.png","expiresIn":900}""",
        failUpload: Boolean = false,
    ): MediaRepository {
        val engine = MockEngine { request -> handle(request, presignBody, confirmBody, downloadBody, failUpload) }
        val secure = InMemorySecureStorage(mapOf(StorageKeys.accessToken to "secret-token"))
        val api = createHttpClient(engine, AppConfig("https://api.test/api/v1", "wss://api.test/ws"), TokenStore(secure), SessionEvents())
        return MediaRepositoryImpl(MediaRemoteDataSourceImpl(api, createMediaUploadClient(engine)), files)
    }

    private fun MockRequestHandleScope.handle(
        request: HttpRequestData,
        presignBody: String,
        confirmBody: String,
        downloadBody: String,
        failUpload: Boolean,
    ): HttpResponseData {
        requests += request
        val path = request.url.encodedPath
        return when {
            path.endsWith("/media/presign") -> respond(presignBody, headers = json)
            path.endsWith("/media/confirm") -> respond(confirmBody, headers = json)
            path.endsWith("/media/download") -> respond(downloadBody, headers = json)
            request.method == HttpMethod.Put && request.url.host == "storage.test" ->
                if (failUpload) respondError(HttpStatusCode.Forbidden) else respond("", HttpStatusCode.OK)
            else -> respondError(HttpStatusCode.NotFound)
        }
    }

    @Test
    fun uploadsWithPresignPutWithoutBearerThenConfirms() = runTest {
        val result = repository().uploadVoice("/data/cache/leo_voice_1.m4a")
        assertEquals("https://cdn.test/voice/k1.m4a", result.getOrNull())

        assertEquals(listOf("/api/v1/media/presign", "/bucket/voice.m4a", "/api/v1/media/confirm"), requests.map { it.url.encodedPath })
        val presign = requests[0]
        assertEquals("Bearer secret-token", presign.headers[HttpHeaders.Authorization])
        assertTrue(presign.body.text().contains("\"mimeType\":\"audio/m4a\""))
        assertTrue(presign.body.text().contains("\"sizeInBytes\":256"))

        val put = requests[1]
        assertEquals(HttpMethod.Put, put.method)
        assertNull(put.headers[HttpHeaders.Authorization], "presigned PUT must not carry the Bearer token")
        assertEquals("audio/mp4", put.body.contentType.toString())
        assertEquals(256L, put.body.contentLength)

        assertEquals("Bearer secret-token", requests[2].headers[HttpHeaders.Authorization])
        assertTrue(requests[2].body.text().contains("\"storageKey\":\"voice/k1\""))
    }

    @Test
    fun remoteUrlsPassThroughWithoutRequests() = runTest {
        val url = "https://cdn.test/already.m4a"
        assertEquals(url, repository().uploadVoice(url).getOrNull())
        assertTrue(requests.isEmpty())
    }

    @Test
    fun missingFileIsValidationFailure() = runTest {
        val result = repository(files = { null }).uploadVoice("/missing.m4a")
        assertIs<AppFailure.Validation>(result.leftOrNull())
        assertTrue(requests.isEmpty())
    }

    @Test
    fun incompletePresignAndMissingConfirmUrlFail() = runTest {
        assertIs<AppFailure.Unknown>(repository(presignBody = """{"uploadUrl":"https://storage.test/x"}""").uploadVoice("/a.m4a").leftOrNull())
        assertIs<AppFailure.Unknown>(repository(confirmBody = "{}").uploadVoice("/a.m4a").leftOrNull())
    }

    @Test
    fun uploadFailureMapsToAppFailure() = runTest {
        val failure = repository(failUpload = true).uploadVoice("/a.m4a").leftOrNull()
        assertIs<AppFailure.Forbidden>(failure)
        assertFalse(requests.any { it.url.encodedPath.endsWith("/media/confirm") })
    }

    @Test
    fun uploadsImageAttachmentAndCachesDownloadUrl() = runTest {
        val repo = repository(
            presignBody = """{"uploadUrl":"https://storage.test/bucket/photo.jpg","storageKey":"img/k2","contentType":"image/jpeg"}""",
            confirmBody = """{"storageKey":"img/k2","url":"https://cdn.test/img/k2.jpg","contentType":"image/jpeg"}""",
        )
        val uploaded = UploadAttachmentUseCase(repo)("content://media/photo/1", "vacation.jpg", "image/jpeg").getOrNull()!!
        assertEquals("img/k2", uploaded.storageKey)
        assertEquals("vacation.jpg", uploaded.fileName)
        assertEquals("https://cdn.test/img/k2.jpg", uploaded.url)
        assertEquals(256L, uploaded.sizeBytes)

        // Subsequent download URL lookup reuses cached URL from confirm without hitting network
        val cachedUrl = GetMediaDownloadUrlUseCase(repo)("img/k2").getOrNull()
        assertEquals("https://cdn.test/img/k2.jpg", cachedUrl)
        assertFalse(requests.any { it.url.encodedPath.endsWith("/media/download") })

        // Uncached key calls GET /media/download?storageKey=... and caches it
        val fetched = GetMediaDownloadUrlUseCase(repo)("img/other").getOrNull()
        assertEquals("https://cdn.test/img/k2.png", fetched)
        assertEquals(1, requests.count { it.url.encodedPath.endsWith("/media/download") })
    }

    @Test
    fun mimeTypeFollowsExtension() {
        assertEquals("audio/wav", mimeTypeForVoice("/x/clip.WAV"))
        assertEquals("audio/m4a", mimeTypeForVoice("/x/clip.m4a"))
    }

    private fun OutgoingContent.text(): String = (this as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()
}

private class FakeMediaRepository(private val result: AppResult<String>) : MediaRepository {
    val uploaded = mutableListOf<String>()
    override suspend fun uploadVoice(filePath: String): AppResult<String> {
        uploaded += filePath
        return result
    }

    override suspend fun uploadAttachment(uriOrPath: String, fileName: String, mimeType: String): AppResult<UploadedAttachment> =
        result.map { url -> UploadedAttachment("key-1", url, fileName, mimeType, 128L) }

    override suspend fun getDownloadUrl(storageKey: String): AppResult<String> = result
}

class MediaUseCasesTest {
    @Test
    fun sendVoiceMessageReturnsVoiceDraft() = runTest {
        val repo = FakeMediaRepository(Either.Right("https://cdn.test/v.m4a"))
        val draft = SendVoiceMessageUseCase(UploadVoiceUseCase(repo))(VoiceRecording("/tmp/v.m4a", 7)).getOrNull()!!
        assertEquals("https://cdn.test/v.m4a", draft.content)
        assertEquals("VOICE", draft.type)
        assertEquals(7, draft.durationSeconds)
        assertEquals(listOf("/tmp/v.m4a"), repo.uploaded)
    }

    @Test
    fun retryFromStoredContentAndFailuresPropagate() = runTest {
        val repo = FakeMediaRepository(Either.Left(AppFailure.Network()))
        val useCase = SendVoiceMessageUseCase(UploadVoiceUseCase(repo))
        assertIs<AppFailure.Network>(useCase("/tmp/v.m4a").leftOrNull())
        assertIs<AppFailure.Validation>(useCase("  ").leftOrNull())
        assertEquals(listOf("/tmp/v.m4a"), repo.uploaded)
    }
}

private class FakeRecorder(var canStart: Boolean = true, var output: String? = "/tmp/rec.m4a") : VoiceRecorder {
    var started = 0
    var cancelled = 0
    override fun start(): Boolean = canStart.also { if (it) started++ }
    override fun stop(): String? = output
    override fun cancel() { cancelled++ }
}

class VoiceRecordingControllerTest {
    @Test
    fun recordsCountsSecondsAndStops() = runTest {
        val recorder = FakeRecorder()
        val controller = VoiceRecordingViewModel(recorder, backgroundScope)
        assertTrue(controller.start())
        assertTrue(controller.state.value.isRecording)
        advanceTimeBy(3_100)
        runCurrent()
        assertEquals(3, controller.state.value.elapsedSeconds)
        val recording = controller.stop()!!
        assertEquals("/tmp/rec.m4a", recording.filePath)
        assertEquals(3, recording.durationSeconds)
        assertEquals("audio/m4a", recording.mimeType)
        assertFalse(controller.state.value.isRecording)
    }

    @Test
    fun shortRecordingIsAtLeastOneSecond() = runTest {
        val controller = VoiceRecordingViewModel(FakeRecorder(), backgroundScope)
        controller.start()
        assertEquals(1, controller.stop()?.durationSeconds)
    }

    @Test
    fun cancelAndUnavailableRecorder() = runTest {
        val recorder = FakeRecorder()
        val controller = VoiceRecordingViewModel(recorder, backgroundScope)
        controller.start()
        controller.cancel()
        assertEquals(1, recorder.cancelled)
        assertFalse(controller.state.value.isRecording)
        assertNull(controller.stop())

        val unavailable = VoiceRecordingViewModel(FakeRecorder(canStart = false), backgroundScope)
        assertFalse(unavailable.start())
        assertFalse(unavailable.state.value.isRecording)
    }

    @Test
    fun emptyRecordingReturnsNull() = runTest {
        val controller = VoiceRecordingViewModel(FakeRecorder(output = null), backgroundScope)
        controller.start()
        assertNull(controller.stop())
    }
}

class VoicePlaybackFormatTest {
    @Test
    fun speedsCycleAndFormat() {
        assertEquals(1.5f, VoicePlaybackSpeeds.next(1f))
        assertEquals(2f, VoicePlaybackSpeeds.next(1.5f))
        assertEquals(1f, VoicePlaybackSpeeds.next(2f))
        assertEquals("1x", VoicePlaybackSpeeds.label(1f))
        assertEquals("1.5x", VoicePlaybackSpeeds.label(1.5f))
        assertEquals("0:18", formatVoiceTime(18_400))
        assertEquals("1:05", formatVoiceTime(65_000))
        assertEquals("01:05", formatRecordingTime(65))
    }
}
