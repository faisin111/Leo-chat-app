package com.leochat.features.settings

import com.leochat.core.constants.SettingsStrings
import com.leochat.core.storage.InMemoryKeyValueStorage
import com.leochat.features.settings.data.datasources.SettingsLocalDataSource
import com.leochat.features.settings.data.repositories.SettingsPreferencesRepositoryImpl
import com.leochat.features.settings.domain.entities.SettingsPreferences
import com.leochat.features.settings.domain.usecases.PasswordChangeValidation
import com.leochat.features.settings.presentation.states.SessionDeviceKind
import com.leochat.features.settings.presentation.states.SessionPresentation
import com.leochat.features.users.domain.entities.UserSessionItem
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SettingsDomainTest {
    private val now = Instant.fromEpochSeconds(1_700_000_000)

    @Test
    fun passwordValidationOrder() {
        assertEquals(PasswordChangeValidation.Error.MissingFields, PasswordChangeValidation.validate("", "a", "a"))
        assertEquals(PasswordChangeValidation.Error.TooShort, PasswordChangeValidation.validate("x", "1234567", "1234567"))
        assertEquals(PasswordChangeValidation.Error.Mismatch, PasswordChangeValidation.validate("x", "12345678", "12345679"))
        assertNull(PasswordChangeValidation.validate("x", "12345678", "12345678"))
    }

    @Test
    fun preferencesRepositoryDefaultsAndRoundTrips() {
        val storage = InMemoryKeyValueStorage()
        val repo = SettingsPreferencesRepositoryImpl(SettingsLocalDataSource(storage))
        assertEquals(SettingsPreferences(), repo.load().getOrNull())
        val changed = SettingsPreferences(messageNotifications = false, messagePreviews = true, callNotifications = false, onlineStatus = false)
        assertTrue(repo.save(changed).isRight)
        assertEquals(changed, repo.load().getOrNull())
    }

    @Test
    fun corruptPreferencesFallBackToDefaults() {
        val storage = InMemoryKeyValueStorage(mapOf(SettingsLocalDataSource.KEY to "{not json"))
        val repo = SettingsPreferencesRepositoryImpl(SettingsLocalDataSource(storage))
        assertEquals(SettingsPreferences(), repo.load().getOrNull())
    }

    @Test
    fun deviceKindMatchesFlutterRules() {
        assertEquals(SessionDeviceKind.Phone, SessionPresentation.deviceKind("iOS", null))
        assertEquals(SessionDeviceKind.Phone, SessionPresentation.deviceKind(null, "Chrome Mobile"))
        assertEquals(SessionDeviceKind.Tablet, SessionPresentation.deviceKind("web", "iPad Safari"))
        assertEquals(SessionDeviceKind.Laptop, SessionPresentation.deviceKind("web", "Firefox"))
    }

    @Test
    fun activityLabels() {
        assertEquals(SettingsStrings.activeRecently, SessionPresentation.activity(null, now))
        assertEquals(SettingsStrings.activeJustNow, SessionPresentation.activity(now - 30.seconds, now))
        assertEquals("Active 15m ago", SessionPresentation.activity(now - 15.minutes, now))
        assertEquals("Active 5h ago", SessionPresentation.activity(now - 5.hours, now))
        assertEquals("Active 3d ago", SessionPresentation.activity(now - 3.days, now))
    }

    @Test
    fun titleSubtitleAndCurrentDevice() {
        val session = UserSessionItem(sessionId = "s", createdAt = now, platform = "android", lastUsedAt = now)
        assertEquals("android", SessionPresentation.title(session))
        assertEquals(SettingsStrings.sessionFallbackName, SessionPresentation.title(session.copy(platform = null)))
        assertEquals("MacBook", SessionPresentation.title(session.copy(deviceName = "MacBook")))
        assertEquals("IP Protected · Active just now", SessionPresentation.subtitle(session, now))
        assertEquals("1.2.3.4 · Active just now", SessionPresentation.subtitle(session.copy(ipAddress = "1.2.3.4"), now))
        assertTrue(SessionPresentation.isCurrent(session, 0))
        assertFalse(SessionPresentation.isCurrent(session, 1))
        assertFalse(SessionPresentation.isCurrent(session.copy(active = false), 0))
    }
}
