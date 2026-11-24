package com.leochat.core.datastore.cache

import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.platform.storage.KeyValueStorage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Cache keys, always scoped by user (and conversation for message pages). */
object CacheKeys {
    private const val prefix = "leo_cache"
    fun userScope(userId: String) = "$prefix:$userId:"
    fun conversations(userId: String) = "${userScope(userId)}conversations"
    fun messages(userId: String, conversationId: String) = "${userScope(userId)}messages:$conversationId"
    fun aiMessages(userId: String) = "${userScope(userId)}ai_messages"
}

/**
 * Local cache for conversation summaries and message pages.
 * Serialization lives here; repositories decide policy. Never stores tokens.
 */
interface CacheStore {
    fun <T> read(key: String, serializer: KSerializer<T>): T?
    fun <T> write(key: String, serializer: KSerializer<T>, value: T)
    fun remove(key: String)
    fun clearUserScope(userId: String)
    fun clearAll()
}

class KeyValueCacheStore(
    private val storage: KeyValueStorage,
    private val json: Json = LeoJson,
) : CacheStore {
    override fun <T> read(key: String, serializer: KSerializer<T>): T? {
        val raw = storage.getString(key) ?: return null
        return runCatching { json.decodeFromString(serializer, raw) }.getOrNull()
    }

    override fun <T> write(key: String, serializer: KSerializer<T>, value: T) {
        storage.putString(key, json.encodeToString(serializer, value))
    }

    override fun remove(key: String) = storage.remove(key)

    override fun clearUserScope(userId: String) {
        val scope = CacheKeys.userScope(userId)
        storage.keys().filter { it.startsWith(scope) }.forEach(storage::remove)
    }

    override fun clearAll() {
        storage.keys().filter { it.startsWith("leo_cache:") }.forEach(storage::remove)
    }
}
