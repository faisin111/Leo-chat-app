package com.leochat.core.platform.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Tokens and identity in EncryptedSharedPreferences (AES-256). */
class AndroidSecureStorage(context: Context) : SecureStorage {
    private val preferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "leochat_secure_session",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun read(key: String): String? = withContext(Dispatchers.IO) { preferences.getString(key, null) }
    override suspend fun write(key: String, value: String) = withContext(Dispatchers.IO) { preferences.edit().putString(key, value).apply() }
    override suspend fun delete(key: String) = withContext(Dispatchers.IO) { preferences.edit().remove(key).apply() }
}

/** Preferences and cache payloads in plain SharedPreferences. */
class AndroidKeyValueStorage(context: Context) : KeyValueStorage {
    private val preferences = context.getSharedPreferences("leochat_preferences", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)
    override fun putString(key: String, value: String) = preferences.edit().putString(key, value).apply()
    override fun remove(key: String) = preferences.edit().remove(key).apply()
    override fun keys(): Set<String> = preferences.all.keys.toSet()
}
