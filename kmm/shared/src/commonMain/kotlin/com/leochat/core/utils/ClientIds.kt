package com.leochat.core.utils

import kotlin.random.Random

/** Generates client-side idempotency IDs such as `clientMessageId`. */
object ClientIds {
    fun next(prefix: String = "kmm"): String {
        val random = Random.nextLong().toULong().toString(16)
        val time = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString(16)
        return "$prefix-$time-$random"
    }
}
