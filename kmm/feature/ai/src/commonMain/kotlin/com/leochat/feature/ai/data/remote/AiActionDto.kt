package com.leochat.feature.ai.data.remote

import com.leochat.core.network.bool
import com.leochat.core.network.string
import com.leochat.feature.ai.domain.model.AiActionRequest
import com.leochat.feature.ai.domain.model.AiActionResult
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Wire mapping for `POST /ai/actions`. */
object AiActionDto {
    fun toJson(request: AiActionRequest): JsonObject = buildJsonObject {
        put("action", request.action)
        request.params.forEach { (key, value) -> put(key, value) }
        if (request.confirmed) put("confirmed", true)
    }

    fun resultFromJson(json: JsonObject) = AiActionResult(
        action = json.string("action"),
        message = json.string("message"),
        data = when (val data = json["data"]) {
            null, JsonNull -> null
            is JsonPrimitive -> data.content
            else -> data.toString()
        },
        confirmationRequired = json.bool("confirmationRequired") ?: false,
    )
}
