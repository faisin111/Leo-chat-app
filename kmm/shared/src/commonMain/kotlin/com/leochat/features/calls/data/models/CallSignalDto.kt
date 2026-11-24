package com.leochat.features.calls.data.models

import com.leochat.core.network.LeoJson
import com.leochat.core.network.int
import com.leochat.core.network.string
import com.leochat.features.calls.domain.entities.CallSignal
import com.leochat.features.calls.domain.entities.CallType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Wire mapping for call signals. Field names follow `CallNotifier` in Flutter. */
object CallSignalDto {
    fun toJson(signal: CallSignal): String = buildJsonObject {
        put("callId", signal.callId)
        put("conversationId", signal.conversationId)
        signal.targetUserId?.let { put("targetUserId", it) }
        put("action", signal.action)
        signal.callType?.let { put("callType", it.wireName) }
        signal.payload?.let { put("payload", it) }
    }.toString()

    fun fromJson(body: String): CallSignal? {
        val json = runCatching { LeoJson.parseToJsonElement(body) as? JsonObject }.getOrNull() ?: return null
        val action = json.string("action")?.uppercase() ?: return null
        // Payloads may arrive as a JSON string or as a nested object.
        val payload = when (val raw = json["payload"]) {
            null -> null
            is JsonPrimitive -> raw.content.takeIf { raw.isString }
            else -> raw.toString()
        }
        return CallSignal(
            callId = json.string("callId").orEmpty(),
            conversationId = json.string("conversationId").orEmpty(),
            targetUserId = json.string("targetUserId"),
            fromUserId = json.string("fromUserId"),
            fromUsername = json.string("fromUsername"),
            action = action,
            callType = json.string("callType")?.let(CallType::fromWire),
            payload = payload,
        )
    }
}

/** Browser-compatible WebRTC payloads (`RTCSessionDescriptionInit` / `RTCIceCandidateInit`). */
data class SdpPayload(val type: String, val sdp: String) {
    fun encode(): String = buildJsonObject {
        put("type", type)
        put("sdp", sdp)
    }.toString()

    companion object {
        fun decode(value: String): SdpPayload? {
            val json = runCatching { LeoJson.parseToJsonElement(value) as? JsonObject }.getOrNull() ?: return null
            val sdp = json.string("sdp") ?: return null
            return SdpPayload(json.string("type").orEmpty().lowercase(), sdp)
        }
    }
}

data class IceCandidatePayload(val candidate: String, val sdpMid: String?, val sdpMLineIndex: Int) {
    fun encode(): String = buildJsonObject {
        put("candidate", candidate)
        sdpMid?.let { put("sdpMid", it) }
        put("sdpMLineIndex", sdpMLineIndex)
    }.toString()

    companion object {
        fun decode(value: String): IceCandidatePayload? {
            val json = runCatching { LeoJson.parseToJsonElement(value) as? JsonObject }.getOrNull() ?: return null
            val candidate = json.string("candidate") ?: return null
            return IceCandidatePayload(candidate, json.string("sdpMid"), json.int("sdpMLineIndex") ?: 0)
        }
    }
}
