package com.leochat.core.common.util

import kotlinx.datetime.Instant

/** Lenient ISO-8601 parse: null or malformed input returns null. */
fun parseInstant(value: String?): Instant? = value?.let { runCatching { Instant.parse(it) }.getOrNull() }
