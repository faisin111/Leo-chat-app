package com.leochat.feature.calls

import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.component.call.model.IncomingCallUi
import com.leochat.feature.calls.domain.model.CallSession
import com.leochat.feature.calls.domain.model.CallStatus
import com.leochat.feature.calls.domain.model.CallType
import com.leochat.feature.calls.presentation.mapper.formatCallDuration
import com.leochat.feature.calls.presentation.mapper.statusText
import com.leochat.feature.calls.presentation.mapper.timerText
import com.leochat.feature.calls.presentation.mapper.toIncomingCallUi
import kotlin.test.Test
import kotlin.test.assertEquals

class CallUiMapperTest {
    private fun session(status: CallStatus, type: CallType = CallType.AUDIO, seconds: Int = 0) = CallSession(
        callId = "c1",
        conversationId = "conv",
        targetUserId = "u2",
        targetUserName = "Ada",
        callType = type,
        status = status,
        isCaller = false,
        targetAvatarUrl = "https://cdn/ada.png",
        connectedSeconds = seconds,
    )

    @Test
    fun incomingCallMapsCallerNameAvatarAndVideoFlag() {
        assertEquals(
            IncomingCallUi(callerName = "Ada", callerAvatarUrl = "https://cdn/ada.png", isVideo = true),
            session(CallStatus.INCOMING, CallType.VIDEO).toIncomingCallUi(),
        )
        assertEquals(false, session(CallStatus.INCOMING).toIncomingCallUi().isVideo)
    }

    @Test
    fun statusTextFollowsCallProgress() {
        assertEquals(CallStrings.connected, session(CallStatus.CONNECTED).statusText())
        assertEquals(CallStrings.calling, session(CallStatus.OUTGOING).statusText())
        assertEquals(CallStrings.connecting, session(CallStatus.CONNECTING).statusText())
    }

    @Test
    fun timerTextShowsDurationOnlyOnceConnected() {
        assertEquals("01:05", session(CallStatus.CONNECTED, seconds = 65).timerText())
        assertEquals(CallStrings.calling, session(CallStatus.OUTGOING, seconds = 65).timerText())
        assertEquals(CallStrings.connecting, session(CallStatus.INCOMING).timerText())
    }

    @Test
    fun callDurationIsZeroPaddedMmSs() {
        assertEquals("00:00", formatCallDuration(0))
        assertEquals("10:09", formatCallDuration(609))
    }
}
