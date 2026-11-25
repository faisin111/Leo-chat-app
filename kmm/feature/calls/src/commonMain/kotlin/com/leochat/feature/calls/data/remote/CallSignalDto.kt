package com.leochat.feature.calls.data.remote

import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.network.int
import com.leochat.core.network.string
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.domain.model.CallType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Wire mapping for call signals matching `wsService.sendCallSignal` in Web:
 * top-level `sdp` (for OFFER/ANSWER) and `candidate`/`sdpMid`/`sdpMLineIndex`
 * (for ICE_CANDIDATE), while tolerating legacy `payload`/`callType` on decode.
 */
object CallSignalDto {
    fun toJson(signal: CallSignal): String = buildJsonObject {
        put("callId", signal.callId)
        put("conversationId", signal.conversationId)
        signal.targetUserId?.let { put("targetUserId", it) }
        put("action", signal.action)
        when (signal.action.uppercase()) {
            CallSignalAction.Offer, CallSignalAction.Answer -> {
                val rawSdp = signal.payload?.let { SdpPayload.decode(it)?.sdp ?: it }
                rawSdp?.let { put("sdp", it) }
            }
            CallSignalAction.IceCandidate -> {
                val ice = signal.payload?.let(IceCandidatePayload::decode)
                if (ice != null) {
                    put("candidate", ice.candidate)
                    ice.sdpMid?.let { put("sdpMid", it) }
                    put("sdpMLineIndex", ice.sdpMLineIndex)
                } else {
                    signal.payload?.let { put("candidate", it) }
                }
            }
        }
    }.toString()

    fun fromJson(body: String): CallSignal? {
        val json = runCatching { LeoJson.parseToJsonElement(body) as? JsonObject }.getOrNull() ?: return null
        val action = json.string("action")?.uppercase() ?: return null
        // Payloads may arrive as top-level Web fields (`sdp`, `candidate`, ...) or nested `payload`.
        val rawPayload = when (val raw = json["payload"]) {
            null -> null
            is JsonPrimitive -> raw.content.takeIf { raw.isString }
            else -> raw.toString()
        }
        val topSdp = json.string("sdp")
        val topCandidate = json.string("candidate")
        val topSdpMid = json.string("sdpMid")
        val topSdpMLineIndex = json.int("sdpMLineIndex") ?: 0

        val payload = when {
            rawPayload != null -> rawPayload
            topSdp != null -> SdpPayload(
                type = if (action == CallSignalAction.Answer) "answer" else "offer",
                sdp = topSdp,
            ).encode()
            topCandidate != null -> IceCandidatePayload(
                candidate = topCandidate,
                sdpMid = topSdpMid,
                sdpMLineIndex = topSdpMLineIndex,
            ).encode()
            else -> null
        }

        val sdpForVideoCheck = topSdp ?: rawPayload?.let { SdpPayload.decode(it)?.sdp ?: it }
        val callType = json.string("callType")?.let(CallType::fromWire)
            ?: if (sdpForVideoCheck?.contains("m=video") == true) CallType.VIDEO else null

        return CallSignal(
            callId = json.string("callId").orEmpty(),
            conversationId = json.string("conversationId").orEmpty(),
            targetUserId = json.string("targetUserId"),
            fromUserId = json.string("fromUserId"),
            fromUsername = json.string("fromUsername"),
            action = action,
            callType = callType,
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
            val json = runCatching { LeoJson.parseToJsonElement(value) as? JsonObject }.getOrNull()
            if (json != null) {
                val sdp = json.string("sdp") ?: return null
                return SdpPayload(json.string("type").orEmpty().lowercase(), sdp)
            }
            return value.takeIf { it.trimStart().startsWith("v=") }?.let { SdpPayload("", it) }
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
