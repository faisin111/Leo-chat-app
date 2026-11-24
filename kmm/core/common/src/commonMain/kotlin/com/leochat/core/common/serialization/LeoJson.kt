package com.leochat.core.common.serialization

import kotlinx.serialization.json.Json

/** Lenient JSON shared by REST, STOMP, and cache serialization. */
val LeoJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
    coerceInputValues = true
    encodeDefaults = true
}
