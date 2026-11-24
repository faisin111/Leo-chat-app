package com.leochat.features.chat

import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.auth.FakeAuthRepository
import com.leochat.features.auth.domain.usecases.GetCurrentUserUseCase
import com.leochat.features.auth.domain.usecases.LogoutUseCase
import com.leochat.features.chat.domain.entities.MessageSendStatus
import com.leochat.features.chat.domain.usecases.ChatUseCaseBundle
import com.leochat.features.chat.presentation.controllers.ChatController
import com.leochat.features.media.domain.entities.VoiceRecording
import com.leochat.features.media.domain.repositories.MediaRepository
import com.leochat.features.media.domain.usecases.SendVoiceMessageUseCase
import com.leochat.features.media.domain.usecases.UploadVoiceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ChatVoiceTest {
    private val repo = FakeChatRepository()
    private val realtime = FakeRealtime()
    private val auth = FakeAuthRepository()
    private val uploads = mutableListOf<String>()
    private var uploadResult: AppResult<String> = Either.Right("https://cdn.test/v.m4a")

    private val media = object : MediaRepository {
        override suspend fun uploadVoice(filePath: String): AppResult<String> {
            uploads += filePath
            return uploadResult
        }
    }

    private fun TestScope.controller(): ChatController {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return ChatController(
            ChatUseCaseBundle.from(repo),
            realtime,
            GetCurrentUserUseCase(auth),
            LogoutUseCase(auth),
            SendVoiceMessageUseCase(UploadVoiceUseCase(media)),
        )
    }

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun uploadsRecordingThenSendsUrlAsVoiceMessage() = runTest {
        val c = controller()
        advanceUntilIdle()
        c.sendVoice(VoiceRecording("/tmp/a.m4a", 4))
        advanceUntilIdle()
        assertEquals(listOf("/tmp/a.m4a"), uploads)
        assertEquals("https://cdn.test/v.m4a", repo.sent.last())
        assertEquals(MessageSendStatus.Sent, c.state.value.messages.last().sendStatus)
    }

    @Test
    fun failedUploadMarksMessageFailedAndRetryReuploads() = runTest {
        uploadResult = Either.Left(AppFailure.Network("offline"))
        val c = controller()
        advanceUntilIdle()
        c.sendVoice(VoiceRecording("/tmp/a.m4a", 4))
        advanceUntilIdle()
        val failed = c.state.value.messages.last()
        assertEquals(MessageSendStatus.Failed, failed.sendStatus)
        assertEquals("VOICE", failed.type)
        assertEquals(emptyList(), repo.sent)

        uploadResult = Either.Right("https://cdn.test/v.m4a")
        c.retry(failed)
        advanceUntilIdle()
        assertEquals(listOf("/tmp/a.m4a", "/tmp/a.m4a"), uploads)
        assertEquals(listOf("https://cdn.test/v.m4a"), repo.sent)
        // The failed bubble is replaced, not duplicated.
        assertEquals(1, c.state.value.messages.count { it.content == "https://cdn.test/v.m4a" || it.content == "/tmp/a.m4a" })
        assertEquals(MessageSendStatus.Sent, c.state.value.messages.last().sendStatus)
    }
}
