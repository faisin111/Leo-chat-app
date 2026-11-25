package com.leochat.android.push

import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.domain.model.CallType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PushCallPayloadTest {
    @Test
    fun parsesHighPriorityIncomingVideoCallPayload() {
        val event = PushCallPayload.parse(
            mapOf(
                "type" to "INCOMING_CALL",
                "callId" to "call-uuid-1",
                "conversationId" to "conv-1",
                "callerId" to "user-2",
                "callerName" to "Maya",
                "callerAvatarUrl" to "https://cdn.test/maya.png",
                "callType" to "VIDEO",
            ),
        )
        val incoming = assertIs<PushCallEvent.Incoming>(event)
        assertEquals("call-uuid-1", incoming.signal.callId)
        assertEquals("conv-1", incoming.signal.conversationId)
        assertEquals("user-2", incoming.signal.fromUserId)
        assertEquals("Maya", incoming.signal.fromUsername)
        assertEquals("https://cdn.test/maya.png", incoming.signal.fromAvatarUrl)
        assertEquals(CallSignalAction.Invite, incoming.signal.action)
        assertEquals(CallType.VIDEO, incoming.signal.callType)
    }

    @Test
    fun parsesSnakeCaseAndCancelEvents() {
        val cancel = PushCallPayload.parse(
            mapOf(
                "notificationType" to "CALL_CANCELLED",
                "call_id" to "call-2",
                "conversation_id" to "conv-2",
            ),
        )
        assertEquals(PushCallEvent.Cancel("call-2", "conv-2"), cancel)

        val audio = PushCallPayload.parse(
            mapOf(
                "action" to "INCOMING_CALL",
                "call_id" to "call-3",
                "conversation_id" to "conv-3",
                "from_user_id" to "user-9",
            ),
            notificationTitle = "Alex",
        )
        val incoming = assertIs<PushCallEvent.Incoming>(audio)
        assertEquals("Alex", incoming.signal.fromUsername)
        assertEquals(CallType.AUDIO, incoming.signal.callType)
        assertNull(PushCallPayload.parse(emptyMap()))
    }
}
