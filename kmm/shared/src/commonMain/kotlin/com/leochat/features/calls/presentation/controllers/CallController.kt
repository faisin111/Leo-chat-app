package com.leochat.features.calls.presentation.controllers

import com.leochat.core.constants.CallStrings
import com.leochat.core.utils.ClientIds
import com.leochat.features.calls.domain.entities.CallIceConfig
import com.leochat.features.calls.domain.entities.CallSession
import com.leochat.features.calls.domain.entities.CallSignal
import com.leochat.features.calls.domain.entities.CallSignalAction
import com.leochat.features.calls.domain.entities.CallStatus
import com.leochat.features.calls.domain.entities.CallType
import com.leochat.features.calls.domain.repositories.CallMediaEngine
import com.leochat.features.calls.domain.repositories.CallMediaEngineFactory
import com.leochat.features.calls.domain.usecases.ObserveCallSignalsUseCase
import com.leochat.features.calls.domain.usecases.SendCallSignalUseCase
import com.leochat.features.calls.presentation.states.CallState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * App-wide call lifecycle (one instance, Koin `single`). Mirrors `CallNotifier`:
 * OFFER/ANSWER/REJECT/BUSY/END/UNAVAILABLE signaling, plus WebRTC negotiation
 * through [CallMediaEngine] (SDP in OFFER/ANSWER payloads, trickle ICE_CANDIDATE).
 */
class CallController(
    private val observeSignals: ObserveCallSignalsUseCase,
    private val sendSignal: SendCallSignalUseCase,
    private val mediaFactory: CallMediaEngineFactory,
    private val iceConfig: CallIceConfig,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(CallState())
    val state: StateFlow<CallState> = mutableState.asStateFlow()

    private val engineLock = Mutex()
    private var engine: CallMediaEngine? = null
    private var iceJob: Job? = null
    private var timerJob: Job? = null
    private var pendingOffer: Pair<String, String>? = null // callId -> SDP
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
        if (mutableState.value.activeCall != null) return
        val session = CallSession(
            callId = ClientIds.next("call"),
            conversationId = conversationId,
            targetUserId = targetUserId,
            targetUserName = targetUserName,
            callType = callType,
            status = CallStatus.OUTGOING,
            isCaller = true,
            targetAvatarUrl = targetAvatarUrl,
        )
        mutableState.update { it.copy(activeCall = session) }
        scope.launch {
            val offer = withEngine(session) { it.createOffer() }
            sendSignal(signalFor(session, CallSignalAction.Offer, callType = callType, payload = offer))
            forwardLocalIce(session)
        }
    }

    fun acceptIncomingCall() {
        val incoming = mutableState.value.incomingCall ?: return
        val active = incoming.copy(status = CallStatus.CONNECTED)
        mutableState.value = CallState(activeCall = active)
        startTimer()
        scope.launch {
            val offer = pendingOffer?.takeIf { it.first == incoming.callId }?.second
            pendingOffer = null
            val answer = withEngine(active) { media -> offer?.let { media.acceptOffer(it) } }
            sendSignal(signalFor(active, CallSignalAction.Answer, callType = active.callType, payload = answer))
            flushPendingCandidates(active.callId)
            forwardLocalIce(active)
        }
    }

    fun rejectIncomingCall() {
        val incoming = mutableState.value.incomingCall ?: return
        pendingOffer = null
        mutableState.update { it.copy(incomingCall = null) }
        scope.launch { sendSignal(signalFor(incoming, CallSignalAction.Reject)) }
    }

    fun endCall() {
        val active = mutableState.value.activeCall
        if (active != null) scope.launch { sendSignal(signalFor(active, CallSignalAction.End)) }
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

    fun handleSignal(signal: CallSignal) {
        val current = mutableState.value
        when (signal.action.uppercase()) {
            CallSignalAction.Invite, CallSignalAction.Offer -> onOffer(signal, current)
            CallSignalAction.Answer -> {
                val active = current.activeCall ?: return
                if (active.callId != signal.callId && signal.callId.isNotEmpty()) return
                mutableState.update { it.copy(activeCall = active.copy(status = CallStatus.CONNECTED)) }
                startTimer()
                signal.payload?.let { answer -> scope.launch { engineLock.withLock { engine?.applyAnswer(answer) } } }
            }
            CallSignalAction.IceCandidate -> {
                val candidate = signal.payload ?: return
                scope.launch {
                    engineLock.withLock {
                        val media = engine
                        if (media != null && current.activeCall?.callId == signal.callId) {
                            media.addRemoteIceCandidate(candidate)
                        } else {
                            pendingCandidates += signal.callId to candidate
                        }
                    }
                }
            }
            CallSignalAction.Delivered -> Unit // Outgoing call keeps ringing.
            CallSignalAction.Reject, CallSignalAction.Busy, CallSignalAction.End, CallSignalAction.Unavailable -> resetCall()
        }
    }

    private fun onOffer(signal: CallSignal, current: CallState) {
        val active = current.activeCall
        if (active != null) {
            // Already on a call: tell the second caller we are busy.
            if (active.callId != signal.callId) {
                scope.launch {
                    sendSignal(CallSignal(signal.callId, signal.conversationId, signal.fromUserId, action = CallSignalAction.Busy))
                }
            }
            return
        }
        pendingOffer = signal.payload?.let { signal.callId to it }
        mutableState.value = current.copy(
            incomingCall = CallSession(
                callId = signal.callId,
                conversationId = signal.conversationId,
                targetUserId = signal.fromUserId.orEmpty(),
                targetUserName = signal.fromUsername?.takeIf { it.isNotBlank() } ?: CallStrings.defaultCallerName,
                callType = signal.callType ?: CallType.AUDIO,
                status = CallStatus.INCOMING,
                isCaller = false,
            ),
        )
    }

    /** Creates the engine for [session] if needed and runs [block]; media failures never block signaling. */
    private suspend fun <T> withEngine(session: CallSession, block: suspend (CallMediaEngine) -> T?): T? = engineLock.withLock {
        try {
            val media = engine ?: mediaFactory.create(iceConfig).also {
                engine = it
                it.start(session.isVideo)
                it.setMuted(session.isMuted)
                it.setSpeakerOn(session.isSpeakerOn)
            }
            if (mutableState.value.activeCall?.callId != session.callId) return@withLock null
            block(media)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
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
        timerJob?.cancel()
        timerJob = null
        iceJob?.cancel()
        iceJob = null
        pendingOffer = null
        mutableState.value = CallState()
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
