package com.leochat.android.push

import com.leochat.core.common.constants.CallStrings
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.domain.model.CallType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncomingCallCardModelTest {
    @Test
    fun buildsVoiceAndVideoCardModelsFromCallSignal() {
        val voice = IncomingCallCardModel.fromSignal(
            CallSignal(
                callId = "c1",
                conversationId = "conv-1",
                fromUserId = "u1",
                fromUsername = "Maya Lin",
                fromAvatarUrl = "https://cdn.test/maya.jpg",
                action = CallSignalAction.Invite,
                callType = CallType.AUDIO,
            ),
        )
        assertEquals("Maya Lin", voice.callerName)
        assertEquals("ML", voice.callerInitials)
        assertEquals(CallStrings.incomingVoiceCall, voice.subtitle)
        assertFalse(voice.isVideo)
        assertEquals("https://cdn.test/maya.jpg", voice.avatarUrl)

        val video = IncomingCallCardModel.fromSignal(
            CallSignal(
                callId = "c2",
                conversationId = "conv-2",
                fromUserId = "u2",
                fromUsername = "   ",
                fromAvatarUrl = " ",
                action = CallSignalAction.Invite,
                callType = CallType.VIDEO,
            ),
        )
        assertEquals(CallStrings.defaultCallerName, video.callerName)
        assertEquals("CA", video.callerInitials)
        assertEquals(CallStrings.incomingVideoCall, video.subtitle)
        assertTrue(video.isVideo)
        assertEquals(null, video.avatarUrl)
    }

    @Test
    fun computesInitialsAndValidatesHttpAvatarUrls() {
        assertEquals("AL", IncomingCallCardModel.initialsForCaller("Alex"))
        assertEquals("JD", IncomingCallCardModel.initialsForCaller("Jane   Doe"))
        assertEquals("LC", IncomingCallCardModel.initialsForCaller("  "))
        assertTrue(IncomingCallCardModel.isHttpAvatarUrl("https://example.com/a.png"))
        assertTrue(IncomingCallCardModel.isHttpAvatarUrl("HTTP://example.com/a.png"))
        assertFalse(IncomingCallCardModel.isHttpAvatarUrl("/relative/avatar.png"))
        assertFalse(IncomingCallCardModel.isHttpAvatarUrl(null))
    }

    @Test
    fun routesUnlockedOverlayDevicesToFloatingBannerAndOthersToShade() {
        assertEquals(
            IncomingCallPresentationMode.FLOATING_OVERLAY,
            IncomingCallCardModel.resolvePresentationMode(
                isInteractive = true,
                isKeyguardLocked = false,
                canDrawOverlays = true,
            ),
        )
        assertEquals(
            IncomingCallPresentationMode.NOTIFICATION_SHADE,
            IncomingCallCardModel.resolvePresentationMode(
                isInteractive = true,
                isKeyguardLocked = true,
                canDrawOverlays = true,
            ),
        )
        assertEquals(
            IncomingCallPresentationMode.NOTIFICATION_SHADE,
            IncomingCallCardModel.resolvePresentationMode(
                isInteractive = false,
                isKeyguardLocked = false,
                canDrawOverlays = true,
            ),
        )
        assertEquals(
            IncomingCallPresentationMode.NOTIFICATION_SHADE,
            IncomingCallCardModel.resolvePresentationMode(
                isInteractive = true,
                isKeyguardLocked = false,
                canDrawOverlays = false,
            ),
        )
    }
}
