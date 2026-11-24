package com.leochat.feature.chat

import com.leochat.core.common.result.Either
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.usecase.AddGroupMembersUseCase
import com.leochat.feature.chat.domain.usecase.MergeConversationIntoCacheUseCase
import com.leochat.feature.chat.domain.usecase.SharedMediaRules
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ContactsDomainTest {
    private fun msg(type: String = "TEXT", content: String?) = Message("m", "c", type = type, content = content)

    @Test
    fun imagesByTypeOrExtension() {
        assertTrue(SharedMediaRules.isImage(msg("image", "x")))
        assertTrue(SharedMediaRules.isImage(msg("IMAGE_MESSAGE", null)))
        assertTrue(SharedMediaRules.isImage(msg(content = "https://x/PIC.JPEG")))
        assertFalse(SharedMediaRules.isImage(msg(content = "hello")))
    }

    @Test
    fun filesExcludeImagesAndMatchTypesOrExtensions() {
        assertTrue(SharedMediaRules.isFile(msg("VIDEO", "x")))
        assertTrue(SharedMediaRules.isFile(msg(content = "a/b/report.PDF")))
        assertFalse(SharedMediaRules.isFile(msg("IMAGE", "a.pdf")))
        assertFalse(SharedMediaRules.isFile(msg(content = "nothing")))
    }

    @Test
    fun linksOnlyForPlainMessagesWithUrls() {
        assertTrue(SharedMediaRules.hasLink(msg(content = "go to http://a.b")))
        assertFalse(SharedMediaRules.hasLink(msg(content = "https://x/a.png")))
        assertFalse(SharedMediaRules.hasLink(msg(content = "https://x/a.zip")))
        assertFalse(SharedMediaRules.hasLink(msg(content = null)))
    }

    @Test
    fun fileNameAndFirstUrlHelpers() {
        assertEquals("report.pdf", SharedMediaRules.fileName("https://x/docs/report.pdf"))
        assertEquals("plain", SharedMediaRules.fileName("plain"))
        assertEquals("https://leo.chat/a", SharedMediaRules.firstUrl("see https://leo.chat/a now"))
        assertEquals("no url", SharedMediaRules.firstUrl("no url"))
    }

    @Test
    fun mergeIntoCacheReplacesByIdOrPrepends() = runTest {
        val repo = FakeChatRepository()
        repo.cachedConversations = listOf(Conversation("a"), Conversation("b"))
        val merge = MergeConversationIntoCacheUseCase(repo)
        merge(Conversation("b", title = "B2"))
        assertEquals(listOf("a", "b"), repo.cachedConversations.map { it.id })
        assertEquals("B2", repo.cachedConversations[1].title)
        merge(Conversation("new"))
        assertEquals(listOf("new", "a", "b"), repo.cachedConversations.map { it.id })
    }

    @Test
    fun addMembersSkipsEmptyListAndDeduplicates() = runTest {
        val repo = FakeChatRepository()
        val add = AddGroupMembersUseCase(repo)
        assertIs<Either.Right<Unit>>(add("g", emptyList()))
        assertTrue(repo.addedMembers.isEmpty())
        add("g", listOf("u1", "u1", "u2"))
        assertEquals(listOf("g" to listOf("u1", "u2")), repo.addedMembers)
    }
}
