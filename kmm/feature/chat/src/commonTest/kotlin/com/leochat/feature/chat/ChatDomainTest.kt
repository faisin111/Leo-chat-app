package com.leochat.feature.chat

import com.leochat.core.common.util.TimeFormat
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.mergeMessages
import com.leochat.feature.chat.domain.usecase.AiPrompt
import com.leochat.feature.chat.domain.usecase.TypingRules
import com.leochat.feature.chat.presentation.mapper.conversationInitials
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatDomainTest {
    @Test
    fun mergeReplacesByIdOrClientIdAndSortsBySeq() {
        val optimistic = Message("client-1", "c", clientMessageId = "client-1", seq = 10, content = "hi")
        val other = Message("m1", "c", seq = 1)
        val confirmed = Message("srv", "c", clientMessageId = "client-1", seq = 5, content = "hi")
        assertEquals(listOf("m1", "srv"), mergeMessages(listOf(optimistic, other), listOf(confirmed)).map { it.id })
    }

    @Test
    fun effectiveTitlePrefersGroupTitleThenTarget() {
        assertEquals("Team", Conversation("1", title = "Team", type = "GROUP", targetDisplayName = "X").effectiveTitle)
        assertEquals("Maya", Conversation("1", title = "t", targetDisplayName = "Maya").effectiveTitle)
        assertEquals("maya", Conversation("1", targetUsername = "maya").effectiveTitle)
        assertEquals("Conversation", Conversation("1").effectiveTitle)
    }

    @Test
    fun aiPromptPrefix() {
        assertTrue(AiPrompt.isAiCommand(" /AI hello"))
        assertEquals("hello", AiPrompt.strip("/ai   hello"))
        assertFalse(AiPrompt.isAiCommand("hi /ai"))
    }

    @Test
    fun typingFromMeIsIgnored() {
        assertTrue(TypingRules.isFromMe(me, "u1", null, null))
        assertTrue(TypingRules.isFromMe(me, null, "ALEX", null))
        assertTrue(TypingRules.isFromMe(me, null, null, "you"))
        assertFalse(TypingRules.isFromMe(me, "u2", "maya", "Maya"))
    }

    @Test
    fun timeLabels() {
        val zone = TimeZone.UTC
        val now = Instant.parse("2026-11-23T15:00:00Z")
        assertEquals("1:05", TimeFormat.clock(Instant.parse("2026-11-23T13:05:00Z"), zone))
        assertEquals("12:00", TimeFormat.clock(Instant.parse("2026-11-23T00:00:00Z"), zone))
        assertEquals("Yesterday", TimeFormat.relative(Instant.parse("2026-11-22T10:00:00Z"), now, zone))
        assertEquals("11/20", TimeFormat.relative(Instant.parse("2026-11-20T10:00:00Z"), now, zone))
    }

    @Test
    fun initials() {
        assertEquals("MC", conversationInitials("Maya  Chen"))
        assertEquals("T", conversationInitials("team"))
        assertEquals("?", conversationInitials(" "))
    }

    @Test
    fun resolvesDirectTargetUserIdFromTargetDirectKeyAndCreatedBy() {
        val meId = "11111111-1111-1111-1111-111111111111"
        val peerId = "22222222-2222-2222-2222-222222222222"

        assertEquals(peerId, Conversation("c1", directKey = "$meId:$peerId").resolveDirectTargetUserId(meId))
        assertEquals(peerId, Conversation("c1", targetUserId = meId, directKey = "${peerId}_$meId").resolveDirectTargetUserId(meId))
        assertEquals(peerId, Conversation("c1", createdBy = peerId).resolveDirectTargetUserId(meId))
        assertEquals(null, Conversation("c1", createdBy = meId).resolveDirectTargetUserId(meId))
        assertEquals(null, Conversation("g1", type = "GROUP", targetUserId = peerId).resolveDirectTargetUserId(meId))
        assertEquals(null, Conversation("ai", type = "AI", targetUserId = peerId).resolveDirectTargetUserId(meId))
    }
}
