package com.leochat.feature.settings.presentation.mapper

import com.leochat.core.designsystem.component.settings.model.SessionDeviceUi
import com.leochat.core.designsystem.component.settings.model.SessionRowUi
import com.leochat.feature.settings.presentation.SessionDeviceKind
import com.leochat.feature.settings.presentation.SessionPresentation
import com.leochat.feature.settings.presentation.settings.SessionsUiState
import com.leochat.feature.users.domain.model.UserSessionItem
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/** Maps the feature's device classification onto the design-system glyph. */
fun SessionDeviceKind.toUi(): SessionDeviceUi = when (this) {
    SessionDeviceKind.Phone -> SessionDeviceUi.Phone
    SessionDeviceKind.Tablet -> SessionDeviceUi.Tablet
    SessionDeviceKind.Laptop -> SessionDeviceUi.Laptop
}

/** Maps one session at list position [index] onto its display row. */
fun UserSessionItem.toSessionRowUi(
    index: Int,
    revokingId: String?,
    now: Instant = Clock.System.now(),
): SessionRowUi = SessionRowUi(
    id = sessionId,
    title = SessionPresentation.title(this),
    subtitle = SessionPresentation.subtitle(this, now),
    device = SessionPresentation.deviceKind(platform, userAgent).toUi(),
    isCurrent = SessionPresentation.isCurrent(this, index),
    isRevoking = revokingId == sessionId,
)

/** Display rows for every session in [SessionsUiState], in list order. */
fun SessionsUiState.toSessionRows(now: Instant = Clock.System.now()): List<SessionRowUi> =
    sessions.mapIndexed { index, session -> session.toSessionRowUi(index, revokingId, now) }
