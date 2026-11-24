package com.leochat.feature.calls.presentation.mapper

import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.component.call.model.IncomingCallUi
import com.leochat.feature.calls.domain.model.CallSession
import com.leochat.feature.calls.domain.model.CallStatus

/** Caller details shown by the incoming call banner. */
fun CallSession.toIncomingCallUi(): IncomingCallUi =
    IncomingCallUi(callerName = targetUserName, callerAvatarUrl = targetAvatarUrl, isVideo = isVideo)

/** Connected, calling (outgoing), or connecting. */
fun CallSession.statusText(): String = when {
    isConnected -> CallStrings.connected
    status == CallStatus.OUTGOING -> CallStrings.calling
    else -> CallStrings.connecting
}

/** Top-bar chip text: the running duration once connected, otherwise the progress label. */
fun CallSession.timerText(): String = when {
    isConnected -> formatCallDuration(connectedSeconds)
    status == CallStatus.OUTGOING -> CallStrings.calling
    else -> CallStrings.connecting
}

/** Formats seconds as `mm:ss`, like `_formatDuration`. */
fun formatCallDuration(totalSeconds: Int): String {
    val minutes = (totalSeconds / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}
