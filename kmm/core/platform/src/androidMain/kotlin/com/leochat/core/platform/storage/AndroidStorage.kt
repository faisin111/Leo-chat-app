package com.leochat.core.platform.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore

private const val SECURE_PREFS_NAME = "leochat_secure_session"

/**
 * Tokens and identity in EncryptedSharedPreferences (AES-256).
 * Recovers automatically from stale Keystore/backup keysets (e.g. `AEADBadTagException`)
 * by resetting the unreadable keyset instead of crash-looping on startup.
 */
class AndroidSecureStorage(private val context: Context) : SecureStorage {
    private val preferences: SharedPreferences? by lazy { openOrRecoverEncryptedPrefs() }
    private val memoryFallback = java.util.concurrent.ConcurrentHashMap<String, String>()

    override suspend fun read(key: String): String? = withContext(Dispatchers.IO) {
        runCatching { preferences?.getString(key, null) }.getOrNull() ?: memoryFallback[key]
    }

    override suspend fun write(key: String, value: String) = withContext(Dispatchers.IO) {
        val prefs = preferences
        if (prefs != null) {
            prefs.edit().putString(key, value).apply()
        } else {
            memoryFallback[key] = value
        }
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        preferences?.edit()?.remove(key)?.apply()
        memoryFallback.remove(key)
        Unit
    }

    private fun openOrRecoverEncryptedPrefs(): SharedPreferences? {
        runCatching { return createEncryptedPrefs() }
        // Keystore key or backed-up keyset mismatch: reset corrupted prefs & master key, then recreate cleanly.
        runCatching { context.deleteSharedPreferences(SECURE_PREFS_NAME) }
        runCatching {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (ks.containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                ks.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        }
        return runCatching { createEncryptedPrefs() }.getOrNull()
    }

    private fun createEncryptedPrefs(): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        SECURE_PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}

/** Preferences and cache payloads in plain SharedPreferences. */
class AndroidKeyValueStorage(context: Context) : KeyValueStorage {
    private val preferences = context.getSharedPreferences("leochat_preferences", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)
    override fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()
    override fun remove(key: String) = preferences.edit().remove(key).apply()
    override fun keys(): Set<String> = preferences.all.keys.toSet()
}
