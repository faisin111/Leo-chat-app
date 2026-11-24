package com.leochat.core.designsystem.component.settings.model

/** Device glyph shown on a session row. */
enum class SessionDeviceUi { Phone, Tablet, Laptop }

/** Immutable display model for one logged-in device row. */
data class SessionRowUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val device: SessionDeviceUi,
    val isCurrent: Boolean,
    val isRevoking: Boolean,
)
