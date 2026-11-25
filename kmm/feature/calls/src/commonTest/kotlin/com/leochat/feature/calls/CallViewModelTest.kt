package com.leochat.feature.calls

import com.leochat.core.common.constants.CallStrings
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
import com.leochat.feature.calls.domain.repository.CallSoundPlayer
import com.leochat.feature.calls.domain.usecase.ObserveCallSignalsUseCase
import com.leochat.feature.calls.domain.usecase.SendCallSignalUseCase
import com.leochat.feature.calls.presentation.CallController
import com.leochat.feature.calls.presentation.CallNotice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    var sendSuccess = true
    override fun incomingSignals(): Flow<CallSignal> = incoming
    override suspend fun send(signal: CallSignal): Boolean {
        sent += signal
        return sendSuccess
    }
}

private class FakeSoundPlayer : CallSoundPlayer {
    var tickCount = 0
    var outgoingCount = 0
    var incomingCount = 0
    var stopCount = 0
    var endCount = 0
    override fun startOutgoingTick() { tickCount++ }
    override fun startOutgoingRing() { outgoingCount++ }
    override fun startIncomingRing() { incomingCount++ }
    override fun stopRing() { stopCount++ }
    override fun playCallEnd() { endCount++ }
}

private class FakeEngine : CallMediaEngine {
    val local = Channel<String>(Channel.UNLIMITED)
    val remoteCandidates = mutableListOf<String>()
    var appliedAnswer: String? = null
    var acceptedOffer: String? = null
    var mutedFlag = false
    var speaker = false
    var cameraEnabledFlag = true
    var closed = false
    var startedVideo: Boolean? = null
    override val localIceCandidates: Flow<String> = local.receiveAsFlow()
    override val hasLocalVideo = MutableStateFlow(false)
    override val hasRemoteVideo = MutableStateFlow(false)
    override suspend fun start(video: Boolean) {
        startedVideo = video
        hasLocalVideo.value = video
    }
    override suspend fun createOffer(): String = "sdp-offer"
    override suspend fun acceptOffer(offer: String): String { acceptedOffer = offer; return "sdp-answer" }
    override suspend fun applyAnswer(answer: String) { appliedAnswer = answer }
    override suspend fun addRemoteIceCandidate(candidate: String) { remoteCandidates += candidate }
    override fun setMuted(muted: Boolean) { mutedFlag = muted }
    override fun setSpeakerOn(enabled: Boolean) { speaker = enabled }
    override fun setCameraEnabled(enabled: Boolean) {
        cameraEnabledFlag = enabled
        hasLocalVideo.value = enabled
    }
    override fun switchCamera() = Unit
    override fun close() { closed = true }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CallControllerTest {
    private fun TestScope.controller(
        signaling: FakeSignaling,
        engine: FakeEngine = FakeEngine(),
        soundPlayer: FakeSoundPlayer = FakeSoundPlayer(),
    ): CallController {
        val scope: CoroutineScope = backgroundScope
        return CallController(
            ObserveCallSignalsUseCase(signaling),
            SendCallSignalUseCase(signaling),
            { engine },
            CallIceConfig.Default,
            scope,
            soundPlayer,
        ).also { runCurrent() }
    }

    @Test
    fun startCallSendsInviteAndOfferWithUuidAndForwardsLocalIce() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        controller.startCall("conversation", "user", "Alex", CallType.VIDEO)
        runCurrent()
        assertEquals(CallStatus.OUTGOING, controller.state.value.activeCall?.status)
        assertTrue(controller.state.value.hasLocalVideo)
        assertEquals(listOf("INVITE", "OFFER"), signaling.sent.map { it.action })
        val invite = signaling.sent[0]
        val offer = signaling.sent[1]
        assertEquals(invite.callId, offer.callId)
        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        assertTrue(uuidRegex.matches(offer.callId), "Expected UUID v4 callId, got ${offer.callId}")
        assertEquals(CallType.VIDEO, offer.callType)
        assertEquals("sdp-offer", offer.payload)
        assertEquals("sdp-offer", invite.payload)
        assertEquals("user", offer.targetUserId)
        assertEquals(true, engine.startedVideo)

        engine.hasRemoteVideo.value = true
        runCurrent()
        assertTrue(controller.state.value.hasRemoteVideo)

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
        controller.toggleCamera()
        runCurrent()
        val call = controller.state.value.activeCall!!
        assertTrue(call.isMuted)
        assertTrue(call.isSpeakerOn)
        assertEquals(false, call.isCameraFront)
        assertTrue(call.isCameraOff)
        assertEquals(false, controller.state.value.hasLocalVideo)
        assertTrue(engine.mutedFlag)
        assertTrue(engine.speaker)
        assertEquals(false, engine.cameraEnabledFlag)
    }

    @Test
    fun startCallIgnoredWhileCallActive() = runTest {
        val signaling = FakeSignaling()
        val controller = controller(signaling)
        controller.startCall("c1", "u1", "A", CallType.AUDIO)
        controller.startCall("c2", "u2", "B", CallType.AUDIO)
        runCurrent()
        assertEquals("c1", controller.state.value.activeCall?.conversationId)
        assertEquals(listOf("INVITE", "OFFER"), signaling.sent.map { it.action })
    }

    @Test
    fun deliveredTransitionsOutgoingToRingingAndSoundStopsOnAnswer() = runTest {
        val signaling = FakeSignaling()
        val sound = FakeSoundPlayer()
        val controller = controller(signaling, soundPlayer = sound)
        controller.startCall("c1", "u1", "Ada", CallType.AUDIO)
        runCurrent()
        assertEquals(CallStatus.OUTGOING, controller.state.value.activeCall?.status)
        assertEquals(1, sound.tickCount)
        assertEquals(0, sound.outgoingCount)

        val callId = signaling.sent.first().callId
        signaling.incoming.emit(CallSignal(callId, "c1", action = "DELIVERED"))
        runCurrent()
        assertEquals(CallStatus.RINGING, controller.state.value.activeCall?.status)
        assertEquals(1, sound.outgoingCount)

        // Repeated RINGING or DELIVERED does not restart ringtone
        signaling.incoming.emit(CallSignal(callId, "c1", action = "RINGING"))
        runCurrent()
        assertEquals(1, sound.outgoingCount)

        signaling.incoming.emit(CallSignal(callId, "c1", action = "ANSWER", payload = "sdp-answer"))
        runCurrent()
        assertEquals(CallStatus.CONNECTED, controller.state.value.activeCall?.status)
        assertEquals(1, sound.stopCount)
    }

    @Test
    fun ringingSignalOnOfflinePushTransitionsOutgoingToRingingAndStartsRingback() = runTest {
        val signaling = FakeSignaling()
        val sound = FakeSoundPlayer()
        val controller = controller(signaling, soundPlayer = sound)
        controller.startCall("c1", "u1", "Ada", CallType.VIDEO)
        runCurrent()
        assertEquals(CallStatus.OUTGOING, controller.state.value.activeCall?.status)
        assertEquals(1, sound.tickCount)

        val callId = signaling.sent.first().callId
        signaling.incoming.emit(CallSignal(callId, "c1", action = "RINGING"))
        runCurrent()
        assertEquals(CallStatus.RINGING, controller.state.value.activeCall?.status)
        assertEquals(1, sound.outgoingCount)
    }

    @Test
    fun sendFailureCancelsCallAndEmitsNotice() = runTest {
        val signaling = FakeSignaling().apply { sendSuccess = false }
        val sound = FakeSoundPlayer()
        val controller = controller(signaling, soundPlayer = sound)
        val notice = backgroundScope.async { controller.notices.first() }
        runCurrent()

        controller.startCall("c1", "u1", "Ada", CallType.AUDIO)
        runCurrent()

        assertNull(controller.state.value.activeCall)
        assertEquals(CallNotice(CallStrings.connectionUnavailable, isError = true), notice.await())
        assertTrue(sound.stopCount >= 1)
    }

    @Test
    fun outgoingTimesOutAfter45SecondsAndEndsCall() = runTest {
        val signaling = FakeSignaling()
        val controller = controller(signaling)
        val notice = backgroundScope.async { controller.notices.first() }
        runCurrent()

        controller.startCall("c1", "u1", "Ada", CallType.AUDIO)
        runCurrent()
        advanceTimeBy(45_100L)
        runCurrent()

        assertNull(controller.state.value.activeCall)
        assertEquals("END", signaling.sent.last().action)
        assertEquals(CallNotice(CallStrings.noAnswer), notice.await())
    }

    @Test
    fun secondOfferWhileIncomingRepliesBusy() = runTest {
        val signaling = FakeSignaling()
        val sound = FakeSoundPlayer()
        val controller = controller(signaling, soundPlayer = sound)
        controller.handleSignal(CallSignal("call-1", "c1", fromUserId = "u1", fromUsername = "Ada", action = "OFFER"))
        runCurrent()
        assertEquals(1, sound.incomingCount)

        controller.handleSignal(CallSignal("call-2", "c2", fromUserId = "u2", fromUsername = "Bob", action = "OFFER"))
        runCurrent()
        assertEquals("call-1", controller.state.value.incomingCall?.callId)
        assertEquals("BUSY", signaling.sent.last().action)
        assertEquals("u2", signaling.sent.last().targetUserId)
    }

    @Test
    fun followUpOfferForSameCallIdSuppliesMissingSdpWithoutBusy() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        controller.handleSignal(CallSignal("call-1", "c1", fromUserId = "u1", fromUsername = "Ada", action = "INVITE"))
        controller.handleSignal(
            CallSignal("call-1", "c1", fromUserId = "u1", action = "OFFER", callType = CallType.VIDEO, payload = "sdp-from-offer"),
        )
        runCurrent()
        assertEquals(CallType.VIDEO, controller.state.value.incomingCall?.callType)
        assertTrue(signaling.sent.none { it.action == "BUSY" })

        controller.acceptIncomingCall()
        runCurrent()
        assertEquals("sdp-from-offer", engine.acceptedOffer)
    }

    @Test
    fun pushWakeupAcceptWaitsForDelayedStompOfferSdp() = runTest {
        val signaling = FakeSignaling()
        val engine = FakeEngine()
        val controller = controller(signaling, engine)
        controller.handleSignal(
            CallSignal(
                callId = "call-push-1",
                conversationId = "c1",
                fromUserId = "u1",
                fromUsername = "Ada",
                fromAvatarUrl = "https://img/ada.png",
                action = "INVITE",
                callType = CallType.VIDEO,
                payload = null,
            ),
        )
        assertEquals("https://img/ada.png", controller.state.value.incomingCall?.targetAvatarUrl)

        controller.acceptIncomingCall()
        runCurrent()
        assertNull(engine.acceptedOffer)

        controller.handleSignal(
            CallSignal("call-push-1", "c1", fromUserId = "u1", action = "OFFER", callType = CallType.VIDEO, payload = "delayed-stomp-sdp"),
        )
        runCurrent()
        assertEquals("delayed-stomp-sdp", engine.acceptedOffer)
        assertEquals("sdp-answer", signaling.sent.single { it.action == "ANSWER" }.payload)
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

@OptIn(ExperimentalCoroutinesApi::class)
class CallSignalingRepositoryTest {
    @Test
    fun parsesIncomingFramesAndWebTopLevelFields() = runTest {
        val source = FakeDataSource()
        val repository = CallSignalingRepositoryImpl(source)
        val deferred = backgroundScope.async { repository.incomingSignals().take(3).toList() }
        runCurrent()
        source.frames.emit("not json")
        source.frames.emit("""{"callId":"c1","conversationId":"conv","fromUserId":"u2","fromUsername":"Maya","action":"offer","callType":"VIDEO","payload":"sdp"}""")
        source.frames.emit("""{"callId":"c2","conversationId":"conv","fromUserId":"u3","fromUsername":"Web","action":"OFFER","sdp":"v=0\r\nm=video 9 UDP/TLS/RTP/SAVPF 96"}""")
        source.frames.emit("""{"callId":"c2","conversationId":"conv","action":"ICE_CANDIDATE","candidate":"cand:x","sdpMid":"0","sdpMLineIndex":1}""")
        val received = deferred.await()
        assertEquals("OFFER", received[0].action)
        assertEquals(CallType.VIDEO, received[0].callType)
        assertEquals("Maya", received[0].fromUsername)
        assertEquals("sdp", received[0].payload)

        assertEquals(CallType.VIDEO, received[1].callType)
        assertEquals("v=0\r\nm=video 9 UDP/TLS/RTP/SAVPF 96", SdpPayload.decode(received[1].payload!!)?.sdp)

        val ice = IceCandidatePayload.decode(received[2].payload!!)
        assertEquals("cand:x", ice?.candidate)
        assertEquals("0", ice?.sdpMid)
        assertEquals(1, ice?.sdpMLineIndex)
    }

    @Test
    fun sendEncodesWebTopLevelSignalFields() = runTest {
        val source = FakeDataSource()
        val sdpJson = SdpPayload("offer", "v=0\r\no=- 1 1 IN IP4 127.0.0.1\r\n").encode()
        CallSignalingRepositoryImpl(source).send(
            CallSignal("c1", "conv", targetUserId = "u2", action = "INVITE", callType = CallType.VIDEO, payload = sdpJson),
        )
        CallSignalingRepositoryImpl(source).send(
            CallSignal("c1", "conv", targetUserId = "u2", action = "OFFER", callType = CallType.AUDIO, payload = sdpJson),
        )
        CallSignalingRepositoryImpl(source).send(
            CallSignal("c1", "conv", targetUserId = "u2", action = "ICE_CANDIDATE", payload = IceCandidatePayload("cand:1", "audio", 0).encode()),
        )

        val inviteJson = source.sent[0]
        assertTrue(inviteJson.contains("\"action\":\"INVITE\""))
        assertTrue(inviteJson.contains("\"sdp\":\"v=0"))

        val offerJson = source.sent[1]
        assertTrue(offerJson.contains("\"callId\":\"c1\""))
        assertTrue(offerJson.contains("\"conversationId\":\"conv\""))
        assertTrue(offerJson.contains("\"targetUserId\":\"u2\""))
        assertTrue(offerJson.contains("\"action\":\"OFFER\""))
        assertTrue(offerJson.contains("\"sdp\":\"v=0"))
        assertTrue(!offerJson.contains("\"payload\""))
        assertEquals("v=0\r\no=- 1 1 IN IP4 127.0.0.1\r\n", SdpPayload.decode(CallSignalDto.fromJson(offerJson)?.payload!!)?.sdp)

        val iceJson = source.sent[2]
        assertTrue(iceJson.contains("\"candidate\":\"cand:1\""))
        assertTrue(iceJson.contains("\"sdpMid\":\"audio\""))
        assertTrue(iceJson.contains("\"sdpMLineIndex\":0"))
        assertEquals("cand:1", IceCandidatePayload.decode(CallSignalDto.fromJson(iceJson)?.payload!!)?.candidate)
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
