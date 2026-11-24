package com.leochat.feature.calls

import com.leochat.feature.calls.data.remote.CallSignalDto
import com.leochat.feature.calls.data.remote.IceCandidatePayload
import com.leochat.feature.calls.data.remote.SdpPayload
import com.leochat.feature.calls.data.repository.CallSignalingRepositoryImpl
import com.leochat.feature.calls.data.remote.CallSignalingDataSource
import com.leochat.feature.calls.domain.model.CallIceConfig
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallStatus
import com.leochat.feature.calls.domain.model.CallType
import com.leochat.feature.calls.domain.repository.CallMediaEngine
import com.leochat.feature.calls.domain.repository.CallSignalingRepository
import com.leochat.feature.calls.domain.usecase.ObserveCallSignalsUseCase
import com.leochat.feature.calls.domain.usecase.SendCallSignalUseCase
import com.leochat.feature.calls.presentation.CallController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSignaling : CallSignalingRepository {
    val incoming = MutableSharedFlow<CallSignal>(extraBufferCapacity = 16)
    val sent = mutableListOf<CallSignal>()
    override fun incomingSignals(): Flow<CallSignal> = incoming
    override suspend fun send(signal: CallSignal): Boolean {
        sent += signal
        return true
    }
}

private class FakeEngine : CallMediaEngine {
    val local = Channel<String>(Channel.UNLIMITED)
    val remoteCandidates = mutableListOf<String>()
    var appliedAnswer: String? = null
    var acceptedOffer: String? = null
    var mutedFlag = false
    var speaker = false
    var closed = false
    var startedVideo: Boolean? = null
    override val localIceCandidates: Flow<String> = local.receiveAsFlow()
    override suspend fun start(video: Boolean) { startedVideo = video }
    override suspend fun createOffer(): String = "sdp-offer"
    override suspend fun acceptOffer(offer: String): String { acceptedOffer = offer; return "sdp-answer" }
    override suspend fun applyAnswer(answer: String) { appliedAnswer = answer }
    override suspend fun addRemoteIceCandidate(candidate: String) { remoteCandidates += candidate }
    override fun setMuted(muted: Boolean) { mutedFlag = muted }
    override fun setSpeakerOn(enabled: Boolean) { speaker = enabled }
    override fun switchCamera() = Unit
    override fun close() { closed = true }
}

class CallControllerTest {
    private fun TestScope.controller(signaling: FakeSignaling, engine: FakeEngine = FakeEngine()): CallController {
        val scope: CoroutineScope = backgroundScope
        return CallController(
            ObserveCallSignalsUseCase(signaling),
            SendCallSignalUseCase(signaling),
            { engine },
            CallIceConfig.Default,
            scope,
        ).also { runCurrent() }
    }

    @Test
    fun startCallSendsOfferWithSdpAndForwardsLocalIce() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        controller.startCall("conversation", "user", "Alex", CallType.VIDEO)
        runCurrent()
        assertEquals(CallStatus.OUTGOING, controller.state.value.activeCall?.status)
        val offer = signaling.sent.single()
        assertEquals("OFFER", offer.action)
        assertEquals(CallType.VIDEO, offer.callType)
        assertEquals("sdp-offer", offer.payload)
        assertEquals("user", offer.targetUserId)
        assertEquals(true, engine.startedVideo)

        engine.local.send("candidate-1")
        runCurrent()
        assertEquals("ICE_CANDIDATE", signaling.sent.last().action)
        assertEquals("candidate-1", signaling.sent.last().payload)
        assertEquals(offer.callId, signaling.sent.last().callId)
    }

    @Test
    fun remoteAnswerConnectsAppliesSdpAndStartsTimer() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        controller.startCall("conversation", "user", "Alex", CallType.AUDIO)
        runCurrent()
        val callId = signaling.sent.first().callId
        signaling.incoming.emit(CallSignal(callId, "conversation", action = "ANSWER", payload = "sdp-answer"))
        runCurrent()
        assertEquals(CallStatus.CONNECTED, controller.state.value.activeCall?.status)
        assertEquals("sdp-answer", engine.appliedAnswer)
        advanceTimeBy(3_100)
        assertEquals(3, controller.state.value.activeCall?.connectedSeconds)
    }

    @Test
    fun incomingOfferBuffersIceThenAcceptAnswersWithSdp() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        signaling.incoming.emit(
            CallSignal("call-1", "conversation", fromUserId = "u2", fromUsername = "Maya", action = "OFFER", callType = CallType.VIDEO, payload = "remote-offer"),
        )
        signaling.incoming.emit(CallSignal("call-1", "conversation", action = "ICE_CANDIDATE", payload = "early-candidate"))
        runCurrent()
        val incoming = assertNotNull(controller.state.value.incomingCall)
        assertEquals("Maya", incoming.targetUserName)
        assertEquals(CallType.VIDEO, incoming.callType)

        controller.acceptIncomingCall()
        runCurrent()
        assertNull(controller.state.value.incomingCall)
        assertEquals(CallStatus.CONNECTED, controller.state.value.activeCall?.status)
        assertEquals("remote-offer", engine.acceptedOffer)
        val answer = signaling.sent.single { it.action == "ANSWER" }
        assertEquals("sdp-answer", answer.payload)
        assertEquals("u2", answer.targetUserId)
        assertEquals(listOf("early-candidate"), engine.remoteCandidates)
    }

    @Test
    fun incomingOfferWithoutNameUsesCallerFallback() = runTest {
        val signaling = FakeSignaling()
        val controller = controller(signaling)
        controller.handleSignal(CallSignal("call", "conversation", fromUserId = "user", action = "INVITE"))
        assertEquals("Caller", controller.state.value.incomingCall?.targetUserName)
        assertEquals(CallType.AUDIO, controller.state.value.incomingCall?.callType)
    }

    @Test
    fun rejectSendsRejectAndClearsIncoming() = runTest {
        val signaling = FakeSignaling()
        val controller = controller(signaling)
        controller.handleSignal(CallSignal("call", "conversation", fromUserId = "user", action = "OFFER"))
        controller.rejectIncomingCall()
        runCurrent()
        assertNull(controller.state.value.incomingCall)
        assertEquals("REJECT", signaling.sent.single().action)
        assertEquals("user", signaling.sent.single().targetUserId)
    }

    @Test
    fun secondOfferWhileOnCallIsAnsweredBusy() = runTest {
        val signaling = FakeSignaling()
        val controller = controller(signaling)
        controller.startCall("conversation", "user", "Alex", CallType.AUDIO)
        runCurrent()
        controller.handleSignal(CallSignal("other", "c2", fromUserId = "u3", action = "OFFER"))
        runCurrent()
        assertNull(controller.state.value.incomingCall)
        assertEquals("BUSY", signaling.sent.last().action)
        assertEquals("u3", signaling.sent.last().targetUserId)
    }

    @Test
    fun endAndRemoteEndClearStateAndCloseEngine() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        controller.startCall("conversation", "user", "Alex", CallType.AUDIO)
        runCurrent()
        controller.endCall()
        runCurrent()
        assertNull(controller.state.value.activeCall)
        assertEquals("END", signaling.sent.last().action)
        assertTrue(engine.closed)

        controller.startCall("conversation", "user", "Alex", CallType.AUDIO)
        runCurrent()
        for (action in listOf("REJECT", "BUSY", "UNAVAILABLE")) {
            assertNotNull(controller.state.value.activeCall)
            controller.handleSignal(CallSignal("x", "conversation", action = action))
            assertNull(controller.state.value.activeCall)
            controller.startCall("conversation", "user", "Alex", CallType.AUDIO)
            runCurrent()
        }
    }

    @Test
    fun togglesUpdateStateAndEngine() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        controller.startCall("conversation", "user", "Alex", CallType.VIDEO)
        runCurrent()
        controller.toggleMute()
        controller.toggleSpeaker()
        controller.flipCamera()
        val call = controller.state.value.activeCall!!
        assertTrue(call.isMuted)
        assertTrue(call.isSpeakerOn)
        assertEquals(false, call.isCameraFront)
        assertTrue(engine.mutedFlag)
        assertTrue(engine.speaker)
    }

    @Test
    fun startCallIgnoredWhileCallActive() = runTest {
        val signaling = FakeSignaling()
        val controller = controller(signaling)
        controller.startCall("c1", "u1", "A", CallType.AUDIO)
        controller.startCall("c2", "u2", "B", CallType.AUDIO)
        runCurrent()
        assertEquals("c1", controller.state.value.activeCall?.conversationId)
        assertEquals(1, signaling.sent.size)
    }
}

private class FakeDataSource : CallSignalingDataSource {
    val frames = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val sent = mutableListOf<String>()
    override fun subscribe(): Flow<String> = frames
    override suspend fun send(jsonBody: String): Boolean {
        sent += jsonBody
        return true
    }
}

class CallSignalingRepositoryTest {
    @Test
    fun parsesIncomingFramesAndDropsMalformed() = runTest {
        val source = FakeDataSource()
        val repository = CallSignalingRepositoryImpl(source)
        val deferred = backgroundScope.async { repository.incomingSignals().take(2).toList() }
        runCurrent()
        source.frames.emit("not json")
        source.frames.emit("""{"callId":"c1","conversationId":"conv","fromUserId":"u2","fromUsername":"Maya","action":"offer","callType":"VIDEO","payload":"sdp"}""")
        source.frames.emit("""{"callId":"c1","conversationId":"conv","action":"ICE_CANDIDATE","payload":{"candidate":"x","sdpMLineIndex":0}}""")
        val received = deferred.await()
        assertEquals("OFFER", received[0].action)
        assertEquals(CallType.VIDEO, received[0].callType)
        assertEquals("Maya", received[0].fromUsername)
        assertEquals("sdp", received[0].payload)
        assertEquals("x", IceCandidatePayload.decode(received[1].payload!!)?.candidate)
    }

    @Test
    fun sendEncodesFlutterFieldNames() = runTest {
        val source = FakeDataSource()
        CallSignalingRepositoryImpl(source).send(
            CallSignal("c1", "conv", targetUserId = "u2", action = "OFFER", callType = CallType.AUDIO, payload = "sdp"),
        )
        val json = source.sent.single()
        assertTrue(json.contains("\"callId\":\"c1\""))
        assertTrue(json.contains("\"targetUserId\":\"u2\""))
        assertTrue(json.contains("\"action\":\"OFFER\""))
        assertTrue(json.contains("\"callType\":\"audio\""))
        assertEquals("sdp", CallSignalDto.fromJson(json)?.payload)
    }

    @Test
    fun observeUseCaseDropsSignalsWithoutCallId() = runTest {
        val signaling = FakeSignaling()
        val deferred = backgroundScope.async { ObserveCallSignalsUseCase(signaling)().first() }
        runCurrent()
        signaling.incoming.emit(CallSignal("", "conv", action = "OFFER"))
        signaling.incoming.emit(CallSignal("ok", "conv", action = "OFFER"))
        val observed = deferred.await()
        assertEquals("ok", observed.callId)
    }

    @Test
    fun webRtcPayloadsRoundTrip() {
        val sdp = SdpPayload("offer", "v=0").encode()
        assertEquals(SdpPayload("offer", "v=0"), SdpPayload.decode(sdp))
        val ice = IceCandidatePayload("candidate:1", "0", 0)
        assertEquals(ice, IceCandidatePayload.decode(ice.encode()))
    }
}
