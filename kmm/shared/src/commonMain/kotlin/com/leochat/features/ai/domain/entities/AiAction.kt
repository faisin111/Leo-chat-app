package com.leochat.features.ai.domain.entities

/** The single AI conversation owned by the signed-in user. */
data class AiConversation(val id: String, val title: String? = null)

/**
 * Request for `POST /ai/actions` (for example SEND_MESSAGE or START_CALL).
 * [params] carries the action arguments; [confirmed] is set when the user approves.
 */
data class AiActionRequest(
    val action: String,
    val params: Map<String, String> = emptyMap(),
    val confirmed: Boolean = false,
)

/** Result of an AI action. When [confirmationRequired] the user must approve before it runs. */
data class AiActionResult(
    val action: String?,
    val message: String?,
    val data: String?,
    val confirmationRequired: Boolean,
)
