package com.leochat.core.common.util

import kotlin.random.Random

/** Generates client-side idempotency IDs such as `clientMessageId` and UUID v4 `callId`. */
object ClientIds {
    fun next(prefix: String = "kmm"): String {
        val random = Random.nextLong().toULong().toString(16)
        val time = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(16)
        return "$prefix-$time-$random"
    }

    /** RFC 4122 random UUID v4 (matches `crypto.randomUUID()` on Web). */
    fun uuid(): String {
        val bytes = Random.nextBytes(16)
        bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
        bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
        val hex = bytes.joinToString("") { b -> (b.toInt() and 0xff).toString(16).padStart(2, '0') }
        return buildString(36) {
            append(hex, 0, 8)
            append('-')
            append(hex, 8, 12)
            append('-')
            append(hex, 12, 16)
            append('-')
            append(hex, 16, 20)
            append('-')
            append(hex, 20, 32)
        }
    }
}
