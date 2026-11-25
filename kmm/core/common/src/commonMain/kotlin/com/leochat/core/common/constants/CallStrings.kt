package com.leochat.core.common.constants

/** User-facing strings for voice and video calls. Mirrors `lib/features/calls`. */
object CallStrings {
    const val defaultCallerName = "Caller"

    // Active call page
    const val leoVideo = "Leo Video"
    const val leoAudio = "Leo Audio"
    const val calling = "Calling..."
    const val ringing = "Ringing..."
    const val connecting = "Connecting..."
    const val connected = "Connected"
    const val frontCameraActive = "Front camera active"
    const val backCameraActive = "Back camera active"
    const val mute = "Mute"
    const val unmute = "Unmute"
    const val speaker = "Speaker"
    const val flip = "Flip"
    const val camera = "Camera"
    const val cameraOff = "Camera off"
    const val youLabel = "You"
    const val waitingForVideo = "Waiting for video..."
    const val endCall = "End call"

    // Call feedback
    const val userUnavailable = "is offline or unavailable"
    const val callDeclined = "declined the call"
    const val userBusy = "is busy on another call"
    const val callEnded = "Call ended"
    const val noAnswer = "No answer"
    const val connectionUnavailable = "Real-time connection is not available"
    const val participantNotFound = "Unable to initiate call: participant not found"
    const val groupCallsUnsupported = "Calling is currently supported for direct 1-on-1 conversations"

    // Incoming call banner
    const val incomingVideoCall = "Incoming Video Call"
    const val incomingVoiceCall = "Incoming Voice Call"
    const val encrypted = "End-to-End Encrypted"
    const val accept = "Accept"
    const val decline = "Decline"

    // Header actions
    const val voiceCall = "Voice call"
    const val videoCall = "Video call"

    // Permissions
    const val microphonePermissionRequired = "Microphone permission required for calls."

    // Background push notification
    const val incomingCallChannelName = "Incoming Calls"
    const val incomingCallChannelDescription = "High-priority alerts for incoming voice and video calls"
    const val ongoingCallChannelName = "Incoming Call Controls"
    const val ongoingCallChannelDescription = "Call notification controls shown while floating popup is active"

    // Floating overlay permission prompt
    const val overlayPermissionTitle = "Enable Floating Call Popup"
    const val overlayPermissionMessage = "Allow LeoChat to display the modern incoming call popup over other apps when your phone is unlocked."
    const val allow = "Allow"
    const val notNow = "Not now"
}
