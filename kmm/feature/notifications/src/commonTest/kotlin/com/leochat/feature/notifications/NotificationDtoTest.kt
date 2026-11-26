package com.leochat.feature.notifications

import com.leochat.core.common.serialization.LeoJson
import com.leochat.feature.notifications.data.remote.AppNotificationDto
import com.leochat.feature.notifications.domain.model.NotificationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificationDtoTest {
    @Test
    fun parsesSpringPageEnvelopeAndNotificationTypes() {
        val json = LeoJson.parseToJsonElement(
            """
            {
              "content": [
                {
                  "id": "n1",
                  "type": "INCOMING_CALL",
                  "title": "Incoming video call",
                  "body": "Maya is calling you",
                  "referenceId": "call-1",
                  "conversationId": "conv-1",
                  "createdAt": "2026-11-25T14:00:00Z",
                  "readAt": null,
                  "read": false
                },
                {
                  "id": "n2",
                  "type": "MESSAGE_REACTION",
                  "title": "Alex",
                  "body": "Reacted ❤️ to your message",
                  "referenceId": "msg-2",
                  "conversationId": "conv-2",
                  "createdAt": "2026-11-25T13:55:00Z",
                  "readAt": "2026-11-25T13:56:00Z",
                  "read": true
                }
              ],
              "totalElements": 12,
              "totalPages": 2,
              "number": 0,
              "size": 30,
              "first": true,
              "last": false
            }
            """.trimIndent(),
        )

        val (dtos, meta) = AppNotificationDto.pageFromJson(json)
        val page = meta.toPage(dtos.map { it.toDomain() })

        assertEquals(2, page.items.size)
        assertEquals(NotificationType.IncomingCall, page.items[0].type)
        assertFalse(page.items[0].isRead)
        assertEquals("conv-1", page.items[0].conversationId)
        assertNull(page.items[0].readAt)

        assertEquals(NotificationType.MessageReaction, page.items[1].type)
        assertTrue(page.items[1].isRead)
        assertEquals(12, page.totalElements)
        assertFalse(page.isLast)
        assertTrue(page.isFirst)
    }

    @Test
    fun parsesNestedSpringPageMetadataFallback() {
        val json = LeoJson.parseToJsonElement(
            """
            {
              "content": [
                {
                  "id": "n3",
                  "type": "NEW_MESSAGE",
                  "title": "Sam",
                  "body": "Hey!",
                  "createdAt": "2026-11-25T14:10:00Z",
                  "read": false
                }
              ],
              "page": {
                "size": 30,
                "number": 1,
                "totalElements": 31,
                "totalPages": 2
              }
            }
            """.trimIndent(),
        )

        val (dtos, meta) = AppNotificationDto.pageFromJson(json)
        assertEquals(1, dtos.size)
        assertEquals(1, meta.pageNumber)
        assertEquals(31, meta.totalElements)
        assertEquals(2, meta.totalPages)
        assertFalse(meta.isFirst)
        assertTrue(meta.isLast)
    }
}
