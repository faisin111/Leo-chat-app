package com.leochat.feature.calls.presentation

import com.leochat.feature.calls.domain.model.CallSession

data class CallUiState(
    val activeCall: CallSession? = null,
    val incomingCall: CallSession? = null,
    val hasLocalVideo: Boolean = false,
    val hasRemoteVideo: Boolean = false,
) {
    val hasActiveCall: Boolean get() = activeCall != null
    val hasIncomingCall: Boolean get() = incomingCall != null
}
