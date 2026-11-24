package com.leochat.feature.settings

import com.leochat.core.designsystem.component.settings.model.SessionDeviceUi
import com.leochat.core.designsystem.component.settings.model.SessionRowUi
import com.leochat.feature.settings.presentation.SessionDeviceKind
import com.leochat.feature.settings.presentation.mapper.toSessionRowUi
import com.leochat.feature.settings.presentation.mapper.toSessionRows
import com.leochat.feature.settings.presentation.mapper.toUi
import com.leochat.feature.settings.presentation.settings.SessionsUiState
import com.leochat.feature.users.domain.model.UserSessionItem
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class SessionRowMapperTest {
    private val now = Instant.parse("2026-01-01T12:00:00Z")

    @Test
    fun mapsEveryDeviceKind() {
        assertEquals(SessionDeviceUi.Phone, SessionDeviceKind.Phone.toUi())
        assertEquals(SessionDeviceUi.Tablet, SessionDeviceKind.Tablet.toUi())
        assertEquals(SessionDeviceUi.Laptop, SessionDeviceKind.Laptop.toUi())
    }

    @Test
    fun mapsCurrentSessionRow() {
        val session = UserSessionItem(
            sessionId = "s1",
            createdAt = now,
            platform = "iOS",
            deviceName = "iPhone",
            ipAddress = "1.2.3.4",
            lastUsedAt = now,
        )
        assertEquals(
            SessionRowUi(
                id = "s1",
                title = "iPhone",
                subtitle = "1.2.3.4 · Active just now",
                device = SessionDeviceUi.Phone,
                isCurrent = true,
                isRevoking = false,
            ),
            session.toSessionRowUi(index = 0, revokingId = null, now = now),
        )
    }

    @Test
    fun onlyFirstActiveRowIsCurrentAndRevokingMatchesId() {
        val first = UserSessionItem(sessionId = "a", createdAt = now, platform = "web", userAgent = "Firefox", lastUsedAt = now - 5.hours)
        val second = first.copy(sessionId = "b", userAgent = "iPad Safari")
        val rows = SessionsUiState(sessions = listOf(first, second), isLoading = false, revokingId = "b").toSessionRows(now)

        assertEquals(listOf("a", "b"), rows.map { it.id })
        assertTrue(rows[0].isCurrent)
        assertFalse(rows[1].isCurrent)
        assertFalse(rows[0].isRevoking)
        assertTrue(rows[1].isRevoking)
        assertEquals(SessionDeviceUi.Laptop, rows[0].device)
        assertEquals(SessionDeviceUi.Tablet, rows[1].device)
        assertEquals("IP Protected · Active 5h ago", rows[0].subtitle)
    }

    @Test
    fun emptyStateMapsToNoRows() {
        assertEquals(emptyList(), SessionsUiState(isLoading = false).toSessionRows(now))
    }
}
