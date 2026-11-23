package com.leochat.features.calls.presentation.controllers

import com.leochat.core.utils.ClientIds
import com.leochat.features.calls.domain.entities.CallSession
import com.leochat.features.calls.domain.entities.CallSignal
import com.leochat.features.calls.domain.entities.CallStatus
import com.leochat.features.calls.domain.entities.CallType
import com.leochat.features.calls.presentation.states.CallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Sends call signals produced by the controller (STOMP in production). */
fun interface CallSignalSink {
    fun send(signal: CallSignal)
}

/**
 * Call lifecycle state machine (offer/answer/ICE/end). Carried over from the
 * earlier KMM slice; UI and WebRTC wiring land with the calls phase.
 */
class CallController(private val signalSink: CallSignalSink) {
    private val mutableState = MutableStateFlow(CallState())
    val state: StateFlow<CallState> = mutableState.asStateFlow()

    fun startCall(conversationId: String, targetUserId: String, targetUserName: String, callType: CallType) {
        val callId = ClientIds.next("call")
        mutableState.value = CallState(
            activeCall = CallSession(callId, conversationId, targetUserId, targetUserName, callType, CallStatus.OUTGOING, isCaller = true),
        )
        signalSink.send(CallSignal(callId, conversationId, targetUserId, action = "OFFER", callType = callType))
    }

    fun acceptIncomingCall() {
        val incoming = mutableState.value.incomingCall ?: return
        mutableState.value = CallState(activeCall = incoming.copy(status = CallStatus.CONNECTED))
        signalSink.send(CallSignal(incoming.callId, incoming.conversationId, incoming.targetUserId, action = "ANSWER"))
    }

    fun rejectIncomingCall() {
        val incoming = mutableState.value.incomingCall ?: return
        signalSink.send(CallSignal(incoming.callId, incoming.conversationId, incoming.targetUserId, action = "REJECT"))
        mutableState.update { it.copy(incomingCall = null) }
    }

    fun endCall() {
        val active = mutableState.value.activeCall ?: return
        signalSink.send(CallSignal(active.callId, active.conversationId, active.targetUserId, action = "END"))
        mutableState.value = CallState()
    }

    fun handleSignal(signal: CallSignal) {
        val current = mutableState.value
        when (signal.action.uppercase()) {
            "INVITE", "OFFER" -> mutableState.value = current.copy(
                incomingCall = CallSession(
                    signal.callId,
                    signal.conversationId,
                    signal.fromUserId.orEmpty(),
                    signal.fromUsername ?: "Caller",
                    signal.callType ?: CallType.AUDIO,
                    CallStatus.INCOMING,
                    isCaller = false,
                ),
            )
            "ANSWER" -> current.activeCall?.let { mutableState.value = current.copy(activeCall = it.copy(status = CallStatus.CONNECTED)) }
            "REJECT", "BUSY", "END", "UNAVAILABLE" -> mutableState.value = CallState()
        }
    }

    fun sendAnswerPayload(payload: String) {
        val active = mutableState.value.activeCall ?: return
        signalSink.send(CallSignal(active.callId, active.conversationId, active.targetUserId, action = "ANSWER", callType = active.callType, payload = payload))
    }

    fun sendIceCandidate(payload: String) {
        val active = mutableState.value.activeCall ?: return
        signalSink.send(CallSignal(active.callId, active.conversationId, active.targetUserId, action = "ICE_CANDIDATE", payload = payload))
    }

    fun toggleMute() = updateActive { it.copy(isMuted = !it.isMuted) }
    fun toggleSpeaker() = updateActive { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    fun flipCamera() = updateActive { it.copy(isCameraFront = !it.isCameraFront) }

    private fun updateActive(transform: (CallSession) -> CallSession) {
        mutableState.update { state -> state.activeCall?.let { state.copy(activeCall = transform(it)) } ?: state }
    }
}
