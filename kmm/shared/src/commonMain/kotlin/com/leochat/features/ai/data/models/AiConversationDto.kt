package com.leochat.features.ai.data.models

import com.leochat.core.network.string
import com.leochat.features.ai.domain.entities.AiConversation
import kotlinx.serialization.json.JsonObject

/** Response of `POST /ai/conversation`. Only the id and title matter to the AI tab. */
data class AiConversationDto(val id: String, val title: String?) {
    fun toDomain() = AiConversation(id, title)

    companion object {
        fun fromJson(json: JsonObject) = AiConversationDto(
            id = json.string("id").orEmpty(),
            title = json.string("title"),
        )
    }
}
