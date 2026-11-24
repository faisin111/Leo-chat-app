package com.leochat.core.network

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/** Lenient field readers for backend payloads whose shapes vary between endpoints. */
fun JsonObject.string(key: String): String? {
    val value: JsonElement = this[key] ?: return null
    if (value is JsonNull) return null
    return (value as? JsonPrimitive)?.content
}

/** First non-blank string among [keys], trimmed. */
fun JsonObject.firstString(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key -> string(key)?.trim()?.takeIf { it.isNotEmpty() } }

fun JsonObject.long(key: String): Long? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return primitive.longOrNull ?: primitive.doubleOrNull?.toLong()
}

fun JsonObject.int(key: String): Int? = long(key)?.toInt()

fun JsonObject.bool(key: String): Boolean? = (this[key] as? JsonPrimitive)?.booleanOrNull

fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject

fun JsonObject.array(key: String): JsonArray? = this[key] as? JsonArray

fun JsonArray?.objects(): List<JsonObject> = this?.mapNotNull { it as? JsonObject }.orEmpty()
