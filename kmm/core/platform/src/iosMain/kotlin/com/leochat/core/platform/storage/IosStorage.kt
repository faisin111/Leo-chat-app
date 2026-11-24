package com.leochat.core.platform.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/** Tokens and identity in the iOS Keychain (generic password items). */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSecureStorage(private val service: String = "com.leochat.secure") : SecureStorage {

    override suspend fun read(key: String): String? = memScoped {
        val query = baseQuery(key)
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        if (status != errSecSuccess) return@memScoped null
        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    override suspend fun write(key: String, value: String) {
        delete(key)
        val query = baseQuery(key)
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(data))
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
        SecItemAdd(query, null)
        CFRelease(query)
    }

    override suspend fun delete(key: String) {
        val query = baseQuery(key)
        SecItemDelete(query)
        CFRelease(query)
    }

    private fun baseQuery(key: String): CFMutableDictionaryRef? {
        val query = CFDictionaryCreateMutable(null, 0, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, CFBridgingRetain(service))
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(key))
        return query
    }
}

/** Preferences and cache payloads in NSUserDefaults. */
class IosKeyValueStorage : KeyValueStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)
    override fun putString(key: String, value: String) = defaults.setObject(value, forKey = key)
    override fun remove(key: String) = defaults.removeObjectForKey(key)
    override fun keys(): Set<String> = defaults.dictionaryRepresentation().keys.mapNotNull { it as? String }.toSet()
}
