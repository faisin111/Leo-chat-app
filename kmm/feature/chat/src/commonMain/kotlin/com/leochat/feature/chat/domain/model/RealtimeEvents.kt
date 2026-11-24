package com.leochat.feature.chat.domain.model

enum class RealtimeStatus { Disconnected, Connecting, Connected }

/** Typing payload from `/topic/conversations.{id}.typing`. Sender fields vary by server version. */
data class TypingEvent(
    val conversationId: String?,
    val userId: String?,
    val username: String?,
    val displayName: String?,
    val isTyping: Boolean,
)

/** Server acknowledgement of a realtime send (`/user/queue/acks`). */
data class MessageAck(
    val conversationId: String,
    val clientMessageId: String,
    val id: String,
    val seq: Long,
)

/** Identity of the signed-in user, used for `isOwn` and typing echo suppression. */
data class CurrentIdentity(val userId: String, val username: String, val displayName: String)
