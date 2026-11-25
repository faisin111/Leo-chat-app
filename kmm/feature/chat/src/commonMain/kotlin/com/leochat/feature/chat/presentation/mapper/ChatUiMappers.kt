package com.leochat.feature.chat.presentation.mapper

import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.common.util.TimeFormat
import com.leochat.core.designsystem.component.chat.model.BubbleSendStatus
import com.leochat.core.designsystem.component.chat.model.ChatBubbleBody
import com.leochat.core.designsystem.component.chat.model.ChatBubbleUi
import com.leochat.core.designsystem.component.chat.model.ConversationHeaderUi
import com.leochat.core.designsystem.component.chat.model.ConversationTileUi
import com.leochat.core.designsystem.component.chat.model.FilterOption
import com.leochat.core.designsystem.component.chat.model.MemberRoleUi
import com.leochat.core.designsystem.component.chat.model.MemberRowUi
import com.leochat.core.designsystem.component.chat.model.PersonRowUi
import com.leochat.core.designsystem.component.chat.model.ReactionUi
import com.leochat.core.designsystem.component.chat.model.RecentContactUi
import com.leochat.core.designsystem.component.chat.model.ReplyQuoteUi
import com.leochat.core.designsystem.component.chat.model.SharedItemUi
import com.leochat.core.designsystem.component.chat.model.SharedMediaThumbUi
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageReply
import com.leochat.feature.chat.domain.model.MessageSendStatus
import com.leochat.feature.chat.domain.usecase.SharedMediaRules
import com.leochat.feature.chat.presentation.ChatFilter
import com.leochat.feature.chat.presentation.GroupDetailsUiState
import com.leochat.feature.chat.presentation.SharedMediaFilter
import com.leochat.feature.users.domain.model.UserSearchItem
import kotlinx.datetime.TimeZone

/** Same fallback as [Message.effectiveSenderName] when a reply has no sender name. */
private const val ReplySenderFallback = "User"

// ---- Messages ----

fun MessageSendStatus.toUi(): BubbleSendStatus = when (this) {
    MessageSendStatus.Sending -> BubbleSendStatus.Sending
    MessageSendStatus.Sent -> BubbleSendStatus.Sent
    MessageSendStatus.Failed -> BubbleSendStatus.Failed
}

fun Message.voiceAudioUrl(resolvedMediaUrls: Map<String, String> = emptyMap()): String? {
    val att = primaryAttachment
    val resolvedFromAtt = att?.localPreviewUrl ?: att?.storageKey?.let { resolvedMediaUrls[it] }
    if (!resolvedFromAtt.isNullOrBlank()) return resolvedFromAtt
    return content?.trim()?.substringBefore('\n')?.trim()?.takeIf { it.isNotEmpty() }
}

fun Message.toBubbleBody(resolvedMediaUrls: Map<String, String> = emptyMap()): ChatBubbleBody {
    val att = primaryAttachment
    val rawContent = content.orEmpty().trim()
    val firstLine = rawContent.substringBefore('\n').trim()
    val directUrl = firstLine.takeIf {
        it.startsWith("http://") || it.startsWith("https://") || it.startsWith("content://") || it.startsWith("file://")
    }
    val resolvedUrl = att?.localPreviewUrl ?: att?.storageKey?.let { resolvedMediaUrls[it] } ?: directUrl.orEmpty()
    val caption = when {
        attachments.isNotEmpty() -> rawContent.takeIf { it.isNotEmpty() }
        rawContent.contains('\n') -> rawContent.substringAfter('\n').trim().takeIf { it.isNotEmpty() }
        directUrl == null -> rawContent.takeIf { it.isNotEmpty() }
        else -> null
    }
    val isResolving = (att != null && resolvedUrl.isBlank()) || sendStatus == MessageSendStatus.Sending

    return when {
        deleted -> ChatBubbleBody.Deleted
        isSystem -> ChatBubbleBody.System(rawContent)
        isCall -> callSummary().let { ChatBubbleBody.Call(it.label, it.isVideo, it.isMissed) }
        isVoice -> ChatBubbleBody.Voice
        isImage -> ChatBubbleBody.Image(
            url = resolvedUrl,
            caption = caption,
            isLoading = isResolving,
        )
        isFile -> ChatBubbleBody.File(
            url = resolvedUrl,
            fileName = att?.fileName?.takeIf { it.isNotBlank() }
                ?: SharedMediaRules.fileName(directUrl ?: rawContent).ifBlank { AppStrings.attachmentFallback },
            sizeBytes = att?.sizeBytes,
            caption = caption,
            isLoading = isResolving,
        )
        else -> ChatBubbleBody.Text(content.orEmpty())
    }
}

fun MessageReply.toQuoteUi(): ReplyQuoteUi =
    ReplyQuoteUi(senderName = senderDisplayName ?: senderUsername ?: ReplySenderFallback, content = content.orEmpty())

/** Bubble model; reply quote and reactions are dropped for deleted messages. */
fun Message.toBubbleUi(
    zone: TimeZone = TimeZone.currentSystemDefault(),
    fallbackSenderName: String? = null,
    resolvedMediaUrls: Map<String, String> = emptyMap(),
): ChatBubbleUi = ChatBubbleUi(
    id = id,
    isOwn = isOwn,
    senderName = when {
        aiGenerated -> AppStrings.aiAssistant
        senderDisplayName?.isNotEmpty() == true -> senderDisplayName
        senderUsername.isNotEmpty() -> senderUsername
        fallbackSenderName?.isNotEmpty() == true -> fallbackSenderName
        else -> "User"
    },
    isAiGenerated = aiGenerated,
    time = TimeFormat.clock(createdAt, zone),
    body = toBubbleBody(resolvedMediaUrls),
    replyTo = replyTo?.takeUnless { deleted }?.toQuoteUi(),
    reactions = if (deleted) emptyList() else reactions.map { ReactionUi(it.emoji, it.count) },
    sendStatus = sendStatus.toUi(),
    isRead = isRead,
)

// ---- Conversations ----

/** First letters of the first and last words, e.g. `Maya Chen` → `MC`. */
fun conversationInitials(value: String): String {
    val words = value.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words.first().take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}

fun Conversation.toTileUi(): ConversationTileUi {
    val title = effectiveTitle
    return ConversationTileUi(
        id = id,
        title = title,
        initials = conversationInitials(title),
        avatarUrl = preferredAvatarUrl,
        isGroup = isGroup,
        isOnline = isOnline,
        snippet = if (isTyping) AppStrings.typingSuffix else lastMessageSnippet ?: AppStrings.noMessagesYet,
        isTyping = isTyping,
        time = TimeFormat.relative(lastMessageAt ?: createdAt, yesterday = AppStrings.yesterday),
        unreadCount = unreadCount,
        isMuted = isMuted,
        isPinned = isPinned,
        isArchived = status.equals("ARCHIVED", ignoreCase = true),
    )
}

fun Conversation.toHeaderUi(): ConversationHeaderUi = ConversationHeaderUi(
    title = if (isAi) AppStrings.leoAiAssistant else effectiveTitle,
    subtitle = when {
        isAi -> AppStrings.aiSubtitleHeader
        isGroup -> AppStrings.groupConversation
        isOnline -> AppStrings.online
        else -> AppStrings.offline
    },
    avatarUrl = preferredAvatarUrl,
    isOnline = isOnline,
    isAi = isAi,
    isGroup = isGroup,
    isPinned = isPinned,
    isMuted = isMuted,
    isArchived = status.equals("ARCHIVED", ignoreCase = true),
)

fun Conversation.toRecentContactUi(): RecentContactUi = RecentContactUi(
    id = id,
    name = effectiveTitle,
    avatarUrl = preferredAvatarUrl,
    isOnline = isOnline,
    snippet = lastMessageSnippet,
)

/** All / Unread (with unread badge) / Groups / Archived. */
fun chatFilterOptions(unreadCount: Int): List<FilterOption<ChatFilter>> = listOf(
    FilterOption(ChatFilter.All, AppStrings.allChats),
    FilterOption(ChatFilter.Unread, AppStrings.unread, badge = unreadCount.takeIf { it > 0 }?.toString()),
    FilterOption(ChatFilter.Groups, AppStrings.groups),
    FilterOption(ChatFilter.Archived, AppStrings.archived),
)

/** Empty conversation list text, quoting the search when there is one. */
fun chatsEmptyMessage(searchQuery: String): String =
    if (searchQuery.isNotEmpty()) "${AppStrings.noChatsMatching} \"$searchQuery\"" else AppStrings.noConversations

// ---- People ----

fun UserSearchItem.toPersonRowUi(): PersonRowUi =
    PersonRowUi(id = id, name = effectiveName, handle = ContactsStrings.handle(username), avatarUrl = profilePictureUrl)

fun ConversationMember.toMemberRowUi(isSelf: Boolean, canRemove: Boolean): MemberRowUi {
    val role = when (role.uppercase()) {
        GroupDetailsUiState.OWNER -> MemberRoleUi.Owner
        GroupDetailsUiState.ADMIN -> MemberRoleUi.Admin
        else -> MemberRoleUi.Member
    }
    return MemberRowUi(
        id = userId,
        name = if (isSelf) ContactsStrings.youSuffix(effectiveName) else effectiveName,
        avatarName = effectiveName,
        handle = username?.let(ContactsStrings::handle),
        role = role,
        canRemove = canRemove,
    )
}

// ---- Shared media ----

fun sharedMediaFilterOptions(mediaCount: Int, filesCount: Int, linksCount: Int): List<FilterOption<SharedMediaFilter>> = listOf(
    FilterOption(SharedMediaFilter.Media, ContactsStrings.mediaCount(mediaCount)),
    FilterOption(SharedMediaFilter.Files, ContactsStrings.filesCount(filesCount)),
    FilterOption(SharedMediaFilter.Links, ContactsStrings.linksCount(linksCount)),
)

fun Message.toSharedThumbUi(): SharedMediaThumbUi = SharedMediaThumbUi(id = id, url = content.orEmpty())

fun Message.toSharedFileUi(): SharedItemUi =
    SharedItemUi(id = id, title = SharedMediaRules.fileName(content ?: ContactsStrings.sharedFile), subtitle = type)

fun Message.toSharedLinkUi(): SharedItemUi {
    val text = content.orEmpty()
    return SharedItemUi(id = id, title = SharedMediaRules.firstUrl(text), subtitle = text)
}
