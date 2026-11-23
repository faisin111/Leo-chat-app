package com.leochat.core.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Time labels used by chat lists and bubbles. Mirrors the Flutter `_formatTime` helpers. */
object TimeFormat {
    /** 12-hour clock without AM/PM, e.g. `9:05`. */
    fun clock(instant: Instant?, zone: TimeZone = TimeZone.currentSystemDefault()): String {
        if (instant == null) return ""
        val t = instant.toLocalDateTime(zone)
        val hour = when {
            t.hour > 12 -> t.hour - 12
            t.hour == 0 -> 12
            else -> t.hour
        }
        return "$hour:${t.minute.toString().padStart(2, '0')}"
    }

    /** Clock for the last day, `Yesterday` for one day ago, otherwise `M/D`. */
    fun relative(
        instant: Instant?,
        now: Instant = Clock.System.now(),
        zone: TimeZone = TimeZone.currentSystemDefault(),
        yesterday: String = "Yesterday",
    ): String {
        if (instant == null) return ""
        val days = (now - instant).inWholeDays
        return when {
            days > 1 -> instant.toLocalDateTime(zone).let { "${it.monthNumber}/${it.dayOfMonth}" }
            days == 1L -> yesterday
            else -> clock(instant, zone)
        }
    }
}
