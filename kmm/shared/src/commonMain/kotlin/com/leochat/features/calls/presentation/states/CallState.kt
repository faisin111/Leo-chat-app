package com.leochat.features.calls.presentation.states

import com.leochat.features.calls.domain.entities.CallSession

data class CallState(
    val activeCall: CallSession? = null,
    val incomingCall: CallSession? = null,
) {
    val hasActiveCall: Boolean get() = activeCall != null
    val hasIncomingCall: Boolean get() = incomingCall != null
}
