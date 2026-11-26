package com.leochat.core.designsystem.component.chat.model

/** Delivery state of an outgoing bubble. */
enum class BubbleSendStatus { Sending, Sent, Failed }

/** What a bubble shows. [Voice] is drawn by the caller's `voiceContent` slot. */
sealed interface ChatBubbleBody {
    data object Deleted : ChatBubbleBody
    data object Voice : ChatBubbleBody
    data class Image(
        val url: String,
        val caption: String? = null,
        val isLoading: Boolean = false,
    ) : ChatBubbleBody
    data class File(
        val url: String,
        val fileName: String,
        val sizeBytes: Long? = null,
        val caption: String? = null,
        val isLoading: Boolean = false,
    ) : ChatBubbleBody
    data class Call(
        val label: String,
        val subtitle: String,
        val isVideo: Boolean,
        val isMissed: Boolean,
        val isOutgoing: Boolean = false,
    ) : ChatBubbleBody
    data class System(val text: String) : ChatBubbleBody
    data class Text(val text: String) : ChatBubbleBody
}

data class ReplyQuoteUi(val senderName: String, val content: String)

data class ReactionUi(val emoji: String, val count: Int) {
    val label: String get() = "$emoji $count"
}

/** One message bubble. [replyTo] and [reactions] are already hidden for deleted messages. */
data class ChatBubbleUi(
    val id: String,
    val isOwn: Boolean,
    val senderName: String,
    val isAiGenerated: Boolean,
    val time: String,
    val body: ChatBubbleBody,
    val replyTo: ReplyQuoteUi? = null,
    val reactions: List<ReactionUi> = emptyList(),
    val sendStatus: BubbleSendStatus = BubbleSendStatus.Sent,
    val isRead: Boolean = false,
)

/** Conversation list row. */
data class ConversationTileUi(
    val id: String,
    val title: String,
    val initials: String,
    val avatarUrl: String?,
    val isGroup: Boolean,
    val isOnline: Boolean,
    val snippet: String,
    val isTyping: Boolean,
    val time: String,
    val unreadCount: Int,
    val isMuted: Boolean,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
) {
    val hasUnread: Boolean get() = unreadCount > 0
    val showsGroupIcon: Boolean get() = isGroup && avatarUrl == null
    val hasOnlineBadge: Boolean get() = !isGroup && isOnline
}

/** Top bar of an open conversation. */
data class ConversationHeaderUi(
    val title: String,
    val subtitle: String,
    val avatarUrl: String?,
    val isOnline: Boolean,
    val isTyping: Boolean = false,
    val isAi: Boolean,
    val isGroup: Boolean,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
)

/** A person in search results, chips, or pickers. [handle] is already formatted (`@name`). */
data class PersonRowUi(
    val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String?,
)

/** A recent direct chat on the new conversation screen. */
data class RecentContactUi(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isOnline: Boolean,
    val snippet: String?,
)

enum class MemberRoleUi { Owner, Admin, Member }

/** Group member row. [name] already carries the "(You)" suffix when it is the current user. */
data class MemberRowUi(
    val id: String,
    val name: String,
    val avatarName: String,
    val handle: String?,
    val role: MemberRoleUi,
    val canRemove: Boolean,
)

/** Shared media thumbnail. */
data class SharedMediaThumbUi(val id: String, val url: String) {
    val isRemote: Boolean get() = url.startsWith("http")
}

/** Shared file or link row. */
data class SharedItemUi(val id: String, val title: String, val subtitle: String)

/** One option of a pill / segmented filter. [badge] is shown next to the label when not null. */
data class FilterOption<T>(val value: T, val label: String, val badge: String? = null)
