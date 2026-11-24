package com.leochat.feature.settings.presentation

import com.leochat.feature.settings.presentation.sessions.SessionsScreen
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.feature.users.domain.model.UserSessionItem
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

enum class SessionDeviceKind { Phone, Tablet, Laptop }

/** Pure formatting for session rows. Mirrors `SessionsScreen._getDeviceIcon` and `_formatRelativeTime`. */
object SessionPresentation {
    fun deviceKind(platform: String?, userAgent: String?): SessionDeviceKind {
        val p = platform.orEmpty().lowercase()
        val ua = userAgent.orEmpty().lowercase()
        return when {
            "ios" in p || "android" in p || "mobile" in ua -> SessionDeviceKind.Phone
            "ipad" in ua || "tablet" in ua -> SessionDeviceKind.Tablet
            else -> SessionDeviceKind.Laptop
        }
    }

    fun activity(lastUsedAt: Instant?, now: Instant = Clock.System.now()): String {
        if (lastUsedAt == null) return SettingsStrings.activeRecently
        val diff = now - lastUsedAt
        return when {
            diff.inWholeMinutes < 2 -> SettingsStrings.activeJustNow
            diff.inWholeHours < 1 -> "${SettingsStrings.activePrefix}${diff.inWholeMinutes}m${SettingsStrings.agoSuffix}"
            diff.inWholeDays < 1 -> "${SettingsStrings.activePrefix}${diff.inWholeHours}h${SettingsStrings.agoSuffix}"
            else -> "${SettingsStrings.activePrefix}${diff.inWholeDays}d${SettingsStrings.agoSuffix}"
        }
    }

    fun title(session: UserSessionItem): String =
        session.deviceName ?: session.platform ?: SettingsStrings.sessionFallbackName

    fun subtitle(session: UserSessionItem, now: Instant = Clock.System.now()): String =
        "${session.ipAddress ?: SettingsStrings.ipProtected}${SettingsStrings.separator}${activity(session.lastUsedAt, now)}"

    /** Flutter marks only the first active row as this device. */
    fun isCurrent(session: UserSessionItem, index: Int): Boolean = session.isCurrentDevice && index == 0
}
