package com.leochat.feature.chat

import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.chat.model.BubbleSendStatus
import com.leochat.core.designsystem.component.chat.model.ChatBubbleBody
import com.leochat.core.designsystem.component.chat.model.MemberRoleUi
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageReaction
import com.leochat.feature.chat.domain.model.MessageReply
import com.leochat.feature.chat.domain.model.MessageSendStatus
import com.leochat.feature.chat.presentation.ChatFilter
import com.leochat.feature.chat.presentation.SharedMediaFilter
import com.leochat.feature.chat.presentation.mapper.chatFilterOptions
import com.leochat.feature.chat.presentation.mapper.chatsEmptyMessage
import com.leochat.feature.chat.presentation.mapper.sharedMediaFilterOptions
import com.leochat.feature.chat.presentation.mapper.toBubbleUi
import com.leochat.feature.chat.presentation.mapper.toHeaderUi
import com.leochat.feature.chat.presentation.mapper.toMemberRowUi
import com.leochat.feature.chat.presentation.mapper.toPersonRowUi
import com.leochat.feature.chat.presentation.mapper.toRecentContactUi
import com.leochat.feature.chat.presentation.mapper.toSharedFileUi
import com.leochat.feature.chat.presentation.mapper.toSharedLinkUi
import com.leochat.feature.chat.presentation.mapper.toSharedThumbUi
import com.leochat.feature.chat.presentation.mapper.toTileUi
import com.leochat.feature.users.domain.model.UserSearchItem
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatUiMappersTest {
    private val reply = MessageReply(id = "r", senderUsername = "maya", content = "earlier")
    private val reaction = MessageReaction("👍", 2)

    @Test
    fun bubbleMapsTextReplyReactionsAndStatus() {
        val message = Message(
            id = "m1",
            conversationId = "c",
            senderUsername = "maya",
            content = "hello",
            createdAt = Instant.parse("2024-01-01T09:05:00Z"),
            replyTo = reply,
            reactions = listOf(reaction),
            sendStatus = MessageSendStatus.Failed,
            isOwn = true,
            isRead = true,
        )
        val ui = message.toBubbleUi(TimeZone.UTC)
        assertEquals(ChatBubbleBody.Text("hello"), ui.body)
        assertEquals("maya", ui.senderName)
        assertEquals("maya", ui.replyTo?.senderName)
        assertEquals("earlier", ui.replyTo?.content)
        assertEquals("👍 2", ui.reactions.single().label)
        assertEquals(BubbleSendStatus.Failed, ui.sendStatus)
        assertTrue(ui.isOwn)
        assertTrue(ui.isRead)
        assertTrue(ui.time.isNotEmpty())
    }

    @Test
    fun deletedBubbleDropsReplyAndReactions() {
        val ui = Message("m", "c", deleted = true, replyTo = reply, reactions = listOf(reaction)).toBubbleUi()
        assertEquals(ChatBubbleBody.Deleted, ui.body)
        assertNull(ui.replyTo)
        assertTrue(ui.reactions.isEmpty())
    }

    @Test
    fun bubbleBodyDetectsVoiceImageFileCallAndSystem() {
        assertEquals(ChatBubbleBody.Voice, Message("v", "c", type = "VOICE", content = "u").toBubbleUi().body)
        assertEquals(ChatBubbleBody.Image("http://x/a.png"), Message("i", "c", type = "IMAGE", content = " http://x/a.png ").toBubbleUi().body)
        assertEquals(
            ChatBubbleBody.File("https://cdn/doc/spec.pdf", "spec.pdf"),
            Message("f", "c", type = "FILE", content = "https://cdn/doc/spec.pdf").toBubbleUi().body,
        )
        assertEquals(
            ChatBubbleBody.Call("Video call", isVideo = true, isMissed = false),
            Message("k1", "c", type = "CALL", content = "Video call").toBubbleUi().body,
        )
        assertEquals(
            ChatBubbleBody.Call("Missed voice call", isVideo = false, isMissed = true),
            Message("k2", "c", type = "CALL", content = "Missed voice call").toBubbleUi().body,
        )
        assertEquals(
            ChatBubbleBody.System("Maya joined"),
            Message("s1", "c", type = "SYSTEM", content = "Maya joined").toBubbleUi().body,
        )
    }

    @Test
    fun previewSnippetFormatsAllMessageTypesAndDeleted() {
        assertEquals(AppStrings.messageDeleted, Message.previewSnippet("TEXT", null, deleted = true))
        assertEquals("Hello", Message.previewSnippet("TEXT", "Hello", deleted = false))
        assertEquals(AppStrings.photoPreview, Message.previewSnippet("IMAGE", "https://cdn/img.png", deleted = false))
        assertEquals(AppStrings.voicePreview, Message.previewSnippet("VOICE", "https://cdn/v.m4a", deleted = false))
        assertEquals("📎 report.pdf", Message.previewSnippet("FILE", "https://cdn/report.pdf", deleted = false))
        assertEquals("📞 Voice call", Message.previewSnippet("CALL", "Voice call", deleted = false))
        assertEquals("📹 Video call", Message.previewSnippet("CALL", "Video call", deleted = false))
        assertEquals("Alex joined", Message.previewSnippet("SYSTEM", "Alex joined", deleted = false))
    }

    @Test
    fun replyWithoutSenderFallsBackToUser() {
        val ui = Message("m", "c", content = "x", replyTo = MessageReply("r")).toBubbleUi()
        assertEquals("User", ui.replyTo?.senderName)
        assertEquals("", ui.replyTo?.content)
    }

    @Test
    fun bubbleSenderUsesFallbackWhenSenderEmpty() {
        val fallbackUi = Message("m", "c", content = "hello").toBubbleUi(fallbackSenderName = "Azad")
        assertEquals("Azad", fallbackUi.senderName)

        val aiUi = Message("m", "c", content = "ai text", aiGenerated = true).toBubbleUi(fallbackSenderName = "Azad")
        assertEquals(AppStrings.aiAssistant, aiUi.senderName)
        assertTrue(aiUi.isAiGenerated)
    }

    @Test
    fun tileUsesTypingSnippetInitialsAndBadges() {
        val typing = Conversation("1", targetDisplayName = "Maya Chen", isTyping = true, isOnline = true, unreadCount = 3).toTileUi()
        assertEquals("Maya Chen", typing.title)
        assertEquals("MC", typing.initials)
        assertEquals(AppStrings.typingSuffix, typing.snippet)
        assertTrue(typing.hasUnread)
        assertTrue(typing.hasOnlineBadge)
        assertFalse(typing.showsGroupIcon)

        val group = Conversation("2", title = "Team", type = "GROUP", isOnline = true).toTileUi()
        assertEquals(AppStrings.noMessagesYet, group.snippet)
        assertTrue(group.showsGroupIcon)
        assertFalse(group.hasOnlineBadge)

        assertEquals("hi", Conversation("3", lastMessageSnippet = "hi").toTileUi().snippet)
    }

    @Test
    fun headerTitleAndSubtitleFollowConversationKind() {
        val ai = Conversation("a", type = "AI").toHeaderUi()
        assertEquals(AppStrings.leoAiAssistant, ai.title)
        assertEquals(AppStrings.aiSubtitleHeader, ai.subtitle)
        assertEquals(AppStrings.groupConversation, Conversation("g", title = "T", type = "GROUP").toHeaderUi().subtitle)
        assertEquals(AppStrings.online, Conversation("d", isOnline = true).toHeaderUi().subtitle)
        assertEquals(AppStrings.offline, Conversation("d").toHeaderUi().subtitle)
    }

    @Test
    fun recentContactAndPersonRows() {
        val contact = Conversation("c", targetUsername = "maya", targetAvatarUrl = "a", isOnline = true, lastMessageSnippet = "s").toRecentContactUi()
        assertEquals("maya", contact.name)
        assertEquals("a", contact.avatarUrl)
        assertTrue(contact.isOnline)
        assertEquals("s", contact.snippet)

        val person = UserSearchItem("u", "maya", displayName = " Maya ", profilePictureUrl = "p").toPersonRowUi()
        assertEquals("Maya", person.name)
        assertEquals("@maya", person.handle)
        assertEquals("p", person.avatarUrl)
    }

    @Test
    fun memberRowMapsRoleSelfSuffixAndHandle() {
        val owner = ConversationMember("c", "u1", role = "owner", username = "maya", displayName = "Maya")
            .toMemberRowUi(isSelf = true, canRemove = false)
        assertEquals(MemberRoleUi.Owner, owner.role)
        assertEquals("Maya (You)", owner.name)
        assertEquals("Maya", owner.avatarName)
        assertEquals("@maya", owner.handle)

        val admin = ConversationMember("c", "u2", role = "ADMIN").toMemberRowUi(isSelf = false, canRemove = true)
        assertEquals(MemberRoleUi.Admin, admin.role)
        assertNull(admin.handle)
        assertTrue(admin.canRemove)

        assertEquals(MemberRoleUi.Member, ConversationMember("c", "u3", role = "MEMBER").toMemberRowUi(false, false).role)
    }

    @Test
    fun filterOptionsCarryLabelsAndUnreadBadge() {
        val options = chatFilterOptions(unreadCount = 4)
        assertEquals(listOf(ChatFilter.All, ChatFilter.Unread, ChatFilter.Groups, ChatFilter.Archived), options.map { it.value })
        assertEquals("4", options[1].badge)
        assertNull(chatFilterOptions(0)[1].badge)

        val shared = sharedMediaFilterOptions(1, 2, 3)
        assertEquals(listOf(SharedMediaFilter.Media, SharedMediaFilter.Files, SharedMediaFilter.Links), shared.map { it.value })
        assertTrue(shared.all { it.badge == null })
    }

    @Test
    fun emptyMessageQuotesSearch() {
        assertEquals(AppStrings.noConversations, chatsEmptyMessage(""))
        assertEquals("${AppStrings.noChatsMatching} \"maya\"", chatsEmptyMessage("maya"))
    }

    @Test
    fun sharedItemsUseMediaRules() {
        assertTrue(Message("m", "c", content = "https://x/a.png").toSharedThumbUi().isRemote)
        assertFalse(Message("m", "c", content = "local").toSharedThumbUi().isRemote)

        val file = Message("f", "c", type = "FILE", content = "dir/report.pdf").toSharedFileUi()
        assertEquals("report.pdf", file.title)
        assertEquals("FILE", file.subtitle)

        val link = Message("l", "c", content = "see https://leo.chat now").toSharedLinkUi()
        assertEquals("https://leo.chat", link.title)
        assertEquals("see https://leo.chat now", link.subtitle)
    }
}
