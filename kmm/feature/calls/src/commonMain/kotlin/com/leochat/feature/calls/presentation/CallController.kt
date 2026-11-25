package com.leochat.feature.calls.presentation

import com.leochat.core.common.constants.CallStrings
import com.leochat.core.common.util.ClientIds
import com.leochat.feature.calls.domain.model.CallIceConfig
import com.leochat.feature.calls.domain.model.CallSession
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.domain.model.CallStatus
import com.leochat.feature.calls.domain.model.CallType
import com.leochat.feature.calls.domain.repository.CallMediaEngine
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import com.leochat.feature.calls.domain.repository.CallSoundPlayer
import com.leochat.feature.calls.domain.repository.NoOpCallSoundPlayer
import com.leochat.feature.calls.domain.usecase.ObserveCallSignalsUseCase
import com.leochat.feature.calls.domain.usecase.SendCallSignalUseCase
import com.leochat.feature.calls.presentation.CallUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CallNotice(val message: String, val isError: Boolean = false)

private const val OUTGOING_TIMEOUT_MS = 45_000L

/**
 * App-wide call lifecycle (one instance, Koin `single`). Mirrors `call.store.ts` in Web:
 * OFFER/DELIVERED/ANSWER/REJECT/BUSY/END/UNAVAILABLE signaling, 45s ring timeout,
 * audio ring feedback, and WebRTC negotiation through [CallMediaEngine].
 */
class CallController(
    private val observeSignals: ObserveCallSignalsUseCase,
    private val sendSignal: SendCallSignalUseCase,
    private val mediaFactory: CallMediaEngineFactory,
    private val iceConfig: CallIceConfig,
    private val scope: CoroutineScope,
    private val soundPlayer: CallSoundPlayer = NoOpCallSoundPlayer,
) {
    private val mutableState = MutableStateFlow(CallUiState())
    val state: StateFlow<CallUiState> = mutableState.asStateFlow()

    private val mutableNotices = MutableSharedFlow<CallNotice>(extraBufferCapacity = 8)
    val notices: SharedFlow<CallNotice> = mutableNotices.asSharedFlow()

    private val engineLock = Mutex()
    private var engine: CallMediaEngine? = null
    private val activeEngineFlow = MutableStateFlow<CallMediaEngine?>(null)
    private var iceJob: Job? = null
    private var videoStateJob: Job? = null
    private var timerJob: Job? = null
    private var outgoingTimeoutJob: Job? = null
    private var pendingOffer: Pair<String, String>? = null // callId -> SDP
    private var pendingOfferWaiter: Pair<String, CompletableDeferred<String>>? = null
    private val pendingCandidates = mutableListOf<Pair<String, String>>() // callId -> candidate

    init {
        scope.launch { observeSignals().collect(::handleSignal) }
    }

    fun startCall(
        conversationId: String,
        targetUserId: String,
        targetUserName: String,
        callType: CallType,
        targetAvatarUrl: String? = null,
    ) {
        if (mutableState.value.activeCall != null || mutableState.value.incomingCall != null) return
        val session = CallSession(
            callId = ClientIds.uuid(),
            conversationId = conversationId,
            targetUserId = targetUserId,
            targetUserName = targetUserName,
            callType = callType,
            status = CallStatus.OUTGOING,
            isCaller = true,
            targetAvatarUrl = targetAvatarUrl,
        )
        mutableState.update { it.copy(activeCall = session, hasLocalVideo = false, hasRemoteVideo = false) }
        soundPlayer.startOutgoingTick()

        outgoingTimeoutJob?.cancel()
        outgoingTimeoutJob = scope.launch {
            delay(OUTGOING_TIMEOUT_MS)
            val current = mutableState.value.activeCall
            if (current?.callId == session.callId && (current.status == CallStatus.OUTGOING || current.status == CallStatus.RINGING)) {
                mutableNotices.tryEmit(CallNotice(CallStrings.noAnswer))
                endCall()
            }
        }

        scope.launch {
            val offer = withEngine(session) { it.createOffer() }
            if (mutableState.value.activeCall?.callId != session.callId) return@launch
            // Send INVITE (triggers backend idempotent CALL message & push/ring) and OFFER (WebRTC SDP), matching Web.
            val invited = sendSignal(signalFor(session, CallSignalAction.Invite, callType = callType, payload = offer))
            val offered = sendSignal(signalFor(session, CallSignalAction.Offer, callType = callType, payload = offer))
            if (!invited && !offered) {
                if (mutableState.value.activeCall?.callId == session.callId) {
                    mutableNotices.tryEmit(CallNotice(CallStrings.connectionUnavailable, isError = true))
                    resetCall()
                }
                return@launch
            }
            forwardLocalIce(session)
        }
    }

    fun acceptIncomingCall() {
        val incoming = mutableState.value.incomingCall ?: return
        soundPlayer.stopRing()
        val active = incoming.copy(status = CallStatus.CONNECTED)
        mutableState.value = CallUiState(activeCall = active)
        startTimer()
        scope.launch {
            var offer = pendingOffer?.takeIf { it.first == incoming.callId }?.second
            pendingOffer = null
            if (offer == null) {
                // When woken from a background push before STOMP delivers the SDP OFFER, wait briefly for it.
                val waiter = CompletableDeferred<String>().also { pendingOfferWaiter = incoming.callId to it }
                offer = withTimeoutOrNull(4_500L) { waiter.await() }
                if (pendingOfferWaiter?.first == incoming.callId) pendingOfferWaiter = null
            }
            val answer = withEngine(active) { media -> offer?.let { media.acceptOffer(it) } }
            sendSignal(signalFor(active, CallSignalAction.Answer, callType = active.callType, payload = answer))
            flushPendingCandidates(active.callId)
            forwardLocalIce(active)
        }
    }

    fun rejectIncomingCall() {
        val incoming = mutableState.value.incomingCall ?: return
        soundPlayer.stopRing()
        pendingOffer = null
        mutableState.update { it.copy(incomingCall = null) }
        scope.launch { sendSignal(signalFor(incoming, CallSignalAction.Reject)) }
    }

    fun endCall() {
        val active = mutableState.value.activeCall
        if (active != null) {
            soundPlayer.playCallEnd()
            scope.launch { sendSignal(signalFor(active, CallSignalAction.End)) }
        }
        resetCall()
    }

    fun toggleMute() {
        val next = updateActive { it.copy(isMuted = !it.isMuted) } ?: return
        engine?.setMuted(next.isMuted)
    }

    fun toggleSpeaker() {
        val next = updateActive { it.copy(isSpeakerOn = !it.isSpeakerOn) } ?: return
        engine?.setSpeakerOn(next.isSpeakerOn)
    }

    fun flipCamera() {
        updateActive { it.copy(isCameraFront = !it.isCameraFront) } ?: return
        engine?.switchCamera()
    }

    fun toggleCamera() {
        val next = updateActive { it.copy(isCameraOff = !it.isCameraOff) } ?: return
        engine?.setCameraEnabled(!next.isCameraOff)
    }

    internal val mediaEngineState: StateFlow<CallMediaEngine?> get() = activeEngineFlow

    fun handleSignal(signal: CallSignal) {
        val current = mutableState.value
        when (signal.action.uppercase()) {
            CallSignalAction.Invite, CallSignalAction.Offer -> onOffer(signal, current)
            CallSignalAction.Delivered -> {
                val active = current.activeCall ?: return
                if (signal.callId.isNotEmpty() && active.callId != signal.callId) return
                if (active.status == CallStatus.OUTGOING) {
                    soundPlayer.startOutgoingRing()
                    mutableState.update { it.copy(activeCall = active.copy(status = CallStatus.RINGING)) }
                }
            }
            CallSignalAction.Answer -> {
                val active = current.activeCall ?: return
                if (active.callId != signal.callId && signal.callId.isNotEmpty()) return
                outgoingTimeoutJob?.cancel()
                outgoingTimeoutJob = null
                soundPlayer.stopRing()
                mutableState.update { it.copy(activeCall = active.copy(status = CallStatus.CONNECTED)) }
                startTimer()
                signal.payload?.let { answer -> scope.launch { engineLock.withLock { engine?.applyAnswer(answer) } } }
            }
            CallSignalAction.IceCandidate -> {
                val candidate = signal.payload ?: return
                scope.launch {
                    engineLock.withLock {
                        val media = engine
                        if (media != null && mutableState.value.activeCall?.callId == signal.callId) {
                            media.addRemoteIceCandidate(candidate)
                        } else {
                            pendingCandidates += signal.callId to candidate
                        }
                    }
                }
            }
            CallSignalAction.Reject -> {
                val name = current.activeCall?.targetUserName ?: signal.fromUsername ?: CallStrings.defaultCallerName
                if (current.activeCall != null) {
                    mutableNotices.tryEmit(CallNotice("$name ${CallStrings.callDeclined}", isError = true))
                }
                resetCall()
            }
            CallSignalAction.Busy -> {
                val name = current.activeCall?.targetUserName ?: signal.fromUsername ?: CallStrings.defaultCallerName
                if (current.activeCall != null) {
                    mutableNotices.tryEmit(CallNotice("$name ${CallStrings.userBusy}", isError = true))
                }
                resetCall()
            }
            CallSignalAction.Unavailable -> {
                val name = current.activeCall?.targetUserName ?: signal.fromUsername ?: CallStrings.defaultCallerName
                if (current.activeCall != null) {
                    mutableNotices.tryEmit(CallNotice("$name ${CallStrings.userUnavailable}", isError = true))
                }
                resetCall()
            }
            CallSignalAction.End -> {
                val hadCall = current.activeCall != null || current.incomingCall != null
                if (hadCall) {
                    soundPlayer.playCallEnd()
                    mutableNotices.tryEmit(CallNotice(CallStrings.callEnded))
                }
                resetCall()
            }
        }
    }

    private fun onOffer(signal: CallSignal, current: CallUiState) {
        val active = current.activeCall
        val existingIncoming = current.incomingCall
        if (active != null || existingIncoming != null) {
            val busyCallId = active?.callId ?: existingIncoming?.callId
            if (busyCallId == signal.callId) {
                // Follow-up OFFER for the same callId (caller sends INVITE then OFFER or push wakeup): store SDP & wake waiter.
                val sdpPayload = signal.payload
                if (!sdpPayload.isNullOrBlank()) {
                    if (pendingOffer == null) pendingOffer = signal.callId to sdpPayload
                    pendingOfferWaiter?.takeIf { it.first == signal.callId }?.second?.complete(sdpPayload)
                }
                if (existingIncoming != null && signal.callType == CallType.VIDEO && existingIncoming.callType != CallType.VIDEO) {
                    mutableState.update { it.copy(incomingCall = existingIncoming.copy(callType = CallType.VIDEO)) }
                }
                return
            }
            // Already on a different call or ringing: tell the second caller we are busy.
            scope.launch {
                sendSignal(CallSignal(signal.callId, signal.conversationId, signal.fromUserId, action = CallSignalAction.Busy))
            }
            return
        }
        pendingOffer = signal.payload?.let { signal.callId to it }
        soundPlayer.startIncomingRing()
        mutableState.value = current.copy(
            incomingCall = CallSession(
                callId = signal.callId,
                conversationId = signal.conversationId,
                targetUserId = signal.fromUserId.orEmpty(),
                targetUserName = signal.fromUsername?.takeIf { it.isNotBlank() } ?: CallStrings.defaultCallerName,
                callType = signal.callType ?: CallType.AUDIO,
                status = CallStatus.INCOMING,
                isCaller = false,
                targetAvatarUrl = signal.fromAvatarUrl,
            ),
        )
    }

    /** Creates the engine for [session] if needed and runs [block]; media failures never block signaling. */
    private suspend fun <T> withEngine(session: CallSession, block: suspend (CallMediaEngine) -> T?): T? = engineLock.withLock {
        try {
            val media = engine ?: mediaFactory.create(iceConfig).also { created ->
                engine = created
                activeEngineFlow.value = created
                created.start(session.isVideo)
                created.setMuted(session.isMuted)
                created.setSpeakerOn(session.isSpeakerOn)
                if (session.isVideo) {
                    created.setCameraEnabled(!session.isCameraOff)
                }
                observeVideoAvailability(session.callId, created)
            }
            if (mutableState.value.activeCall?.callId != session.callId) return@withLock null
            block(media)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
    }

    private fun observeVideoAvailability(callId: String, media: CallMediaEngine) {
        videoStateJob?.cancel()
        videoStateJob = scope.launch {
            launch {
                media.hasLocalVideo.collect { local ->
                    if (mutableState.value.activeCall?.callId == callId) {
                        mutableState.update { it.copy(hasLocalVideo = local) }
                    }
                }
            }
            launch {
                media.hasRemoteVideo.collect { remote ->
                    if (mutableState.value.activeCall?.callId == callId) {
                        mutableState.update { it.copy(hasRemoteVideo = remote) }
                    }
                }
            }
        }
    }

    private fun forwardLocalIce(session: CallSession) {
        val media = engine ?: return
        iceJob?.cancel()
        iceJob = scope.launch {
            media.localIceCandidates.collect { candidate ->
                if (mutableState.value.activeCall?.callId == session.callId) {
                    sendSignal(signalFor(session, CallSignalAction.IceCandidate, payload = candidate))
                }
            }
        }
    }

    private suspend fun flushPendingCandidates(callId: String) = engineLock.withLock {
        val media = engine
        val matching = pendingCandidates.filter { it.first == callId }
        pendingCandidates.clear()
        if (media != null) matching.forEach { runCatching { media.addRemoteIceCandidate(it.second) } }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1_000)
                updateActive { if (it.isConnected) it.copy(connectedSeconds = it.connectedSeconds + 1) else it }
            }
        }
    }

    private fun resetCall() {
        soundPlayer.stopRing()
        outgoingTimeoutJob?.cancel()
        outgoingTimeoutJob = null
        timerJob?.cancel()
        timerJob = null
        iceJob?.cancel()
        iceJob = null
        videoStateJob?.cancel()
        videoStateJob = null
        pendingOffer = null
        pendingOfferWaiter?.second?.cancel()
        pendingOfferWaiter = null
        activeEngineFlow.value = null
        mutableState.value = CallUiState()
        scope.launch {
            engineLock.withLock {
                pendingCandidates.clear()
                engine?.let { runCatching { it.close() } }
                engine = null
            }
        }
    }

    private fun updateActive(transform: (CallSession) -> CallSession): CallSession? {
        var next: CallSession? = null
        mutableState.update { state ->
            state.activeCall?.let { active -> transform(active).also { next = it }.let { state.copy(activeCall = it) } } ?: state
        }
        return next
    }

    private fun signalFor(session: CallSession, action: String, callType: CallType? = null, payload: String? = null) =
        CallSignal(session.callId, session.conversationId, session.targetUserId, action = action, callType = callType, payload = payload)
}
