package com.leochat.features.calls

import com.leochat.features.calls.domain.entities.CallSignal
import com.leochat.features.calls.domain.entities.CallStatus
import com.leochat.features.calls.domain.entities.CallType
import com.leochat.features.calls.presentation.controllers.CallController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CallControllerTest {
    @Test
    fun offerCreatesOutgoingCallAndSignal() {
        var sent: CallSignal? = null
        val controller = CallController { sent = it }
        controller.startCall("conversation", "user", "Alex", CallType.VIDEO)
        assertEquals("OFFER", sent?.action)
        assertEquals(CallStatus.OUTGOING, controller.state.value.activeCall?.status)
    }

    @Test
    fun incomingOfferCanBeAccepted() {
        val controller = CallController { }
        controller.handleSignal(CallSignal("call", "conversation", fromUserId = "user", fromUsername = "Alex", action = "OFFER"))
        assertNotNull(controller.state.value.incomingCall)
        controller.acceptIncomingCall()
        assertEquals(CallStatus.CONNECTED, controller.state.value.activeCall?.status)
    }

    @Test
    fun answerAndIcePayloadsUseActiveCallIdentity() {
        val signals = mutableListOf<CallSignal>()
        val controller = CallController { signals += it }
        controller.startCall("conversation", "user", "Alex", CallType.AUDIO)
        controller.sendAnswerPayload("sdp-answer")
        controller.sendIceCandidate("candidate")
        assertEquals("sdp-answer", signals[1].payload)
        assertEquals("candidate", signals[2].payload)
        assertEquals(signals[0].callId, signals[2].callId)
    }

    @Test
    fun remoteEndClearsState() {
        val controller = CallController { }
        controller.startCall("conversation", "user", "Alex", CallType.AUDIO)
        controller.handleSignal(CallSignal("x", "conversation", action = "END"))
        assertNull(controller.state.value.activeCall)
    }
}
