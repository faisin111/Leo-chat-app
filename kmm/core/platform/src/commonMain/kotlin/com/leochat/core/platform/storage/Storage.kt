package com.leochat.core.platform.storage

/**
 * Encrypted key-value storage for tokens and identity.
 * Android: EncryptedSharedPreferences. iOS: Keychain.
 */
interface SecureStorage {
    suspend fun read(key: String): String?
    suspend fun write(key: String, value: String)
    suspend fun delete(key: String)
}

/**
 * Plain key-value storage for preferences and cache payloads.
 * Never store tokens here. Android: SharedPreferences. iOS: NSUserDefaults.
 */
interface KeyValueStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun keys(): Set<String>
}

class InMemorySecureStorage(initial: Map<String, String> = emptyMap()) : SecureStorage {
    private val values = initial.toMutableMap()
    override suspend fun read(key: String): String? = values[key]
    override suspend fun write(key: String, value: String) { values[key] = value }
    override suspend fun delete(key: String) { values.remove(key) }
}

class InMemoryKeyValueStorage(initial: Map<String, String> = emptyMap()) : KeyValueStorage {
    private val values = initial.toMutableMap()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
    override fun keys(): Set<String> = values.keys.toSet()
}
