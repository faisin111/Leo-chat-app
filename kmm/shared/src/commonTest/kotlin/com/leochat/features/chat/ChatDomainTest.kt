package com.leochat.features.chat

import com.leochat.core.utils.TimeFormat
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.entities.mergeMessages
import com.leochat.features.chat.domain.usecases.AiPrompt
import com.leochat.features.chat.domain.usecases.TypingRules
import com.leochat.features.chat.presentation.widgets.conversationInitials
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
}
