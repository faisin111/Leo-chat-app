package com.leochat.android.push

import com.leochat.core.common.constants.AppStrings
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.domain.model.CallType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PushCallPayloadTest {
    @Test
    fun parsesHighPriorityIncomingVideoCallPayload() {
        val event = PushNotificationPayload.parse(
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
        val incoming = assertIs<PushEvent.IncomingCall>(event)
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
        val cancel = PushNotificationPayload.parse(
            mapOf(
                "notificationType" to "CALL_CANCELLED",
                "call_id" to "call-2",
                "conversation_id" to "conv-2",
            ),
        )
        assertEquals(PushEvent.CancelCall("call-2", "conv-2"), cancel)

        val audio = PushNotificationPayload.parse(
            mapOf(
                "action" to "INCOMING_CALL",
                "call_id" to "call-3",
                "conversation_id" to "conv-3",
                "from_user_id" to "user-9",
            ),
            notificationTitle = "Alex",
        )
        val incoming = assertIs<PushEvent.IncomingCall>(audio)
        assertEquals("Alex", incoming.signal.fromUsername)
        assertEquals(CallType.AUDIO, incoming.signal.callType)
        assertNull(PushNotificationPayload.parse(emptyMap()))
    }

    @Test
    fun parsesNewMessageTypesForTextImageVoiceAndFile() {
        val textMsg = assertIs<PushEvent.NewMessage>(
            PushNotificationPayload.parse(
                mapOf(
                    "type" to "NEW_MESSAGE",
                    "messageId" to "m1",
                    "conversationId" to "c1",
                    "senderId" to "u2",
                    "senderName" to "Maya",
                    "messageType" to "TEXT",
                    "content" to "Hey there!",
                ),
            ),
        )
        assertEquals("Maya", textMsg.displayTitle)
        assertEquals("Hey there!", textMsg.displayBody)
        assertEquals("TEXT", textMsg.messageType)

        val imageMsg = assertIs<PushEvent.NewMessage>(
            PushNotificationPayload.parse(
                mapOf(
                    "type" to "NEW_MESSAGE",
                    "messageId" to "m2",
                    "conversationId" to "c2",
                    "senderDisplayName" to "Alex",
                    "conversationTitle" to "Design Team",
                    "messageType" to "IMAGE",
                ),
            ),
        )
        assertEquals("Design Team", imageMsg.displayTitle)
        assertEquals("Alex: ${AppStrings.photoPreview}", imageMsg.displayBody)

        val voiceMsg = assertIs<PushEvent.NewMessage>(
            PushNotificationPayload.parse(
                mapOf(
                    "type" to "NEW_MESSAGE",
                    "messageId" to "m3",
                    "conversationId" to "c3",
                    "senderName" to "Sam",
                    "messageType" to "VOICE",
                ),
            ),
        )
        assertEquals(AppStrings.voicePreview, voiceMsg.displayBody)

        val fileMsg = assertIs<PushEvent.NewMessage>(
            PushNotificationPayload.parse(
                mapOf(
                    "type" to "NEW_MESSAGE",
                    "messageId" to "m4",
                    "conversationId" to "c4",
                    "senderName" to "Sam",
                    "messageType" to "FILE",
                    "content" to "spec.pdf",
                ),
            ),
        )
        assertEquals("📎 spec.pdf", fileMsg.displayBody)
    }

    @Test
    fun parsesMessageReactionNotifications() {
        val reaction = assertIs<PushEvent.MessageReaction>(
            PushNotificationPayload.parse(
                mapOf(
                    "type" to "MESSAGE_REACTION",
                    "messageId" to "m9",
                    "conversationId" to "c9",
                    "senderName" to "Maya",
                    "emoji" to "❤️",
                    "content" to "See you at 5",
                ),
            ),
        )
        assertEquals("Maya", reaction.displayTitle)
        assertEquals("Reacted ❤️ to: See you at 5", reaction.displayBody)
        assertEquals("m9", reaction.messageId)
        assertEquals("c9", reaction.conversationId)
    }
}
