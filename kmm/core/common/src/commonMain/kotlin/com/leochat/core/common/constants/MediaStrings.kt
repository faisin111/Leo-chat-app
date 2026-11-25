package com.leochat.core.common.constants

/** User-facing strings for voice recording, playback, and uploads. Mirrors `lib/features/media`. */
object MediaStrings {
    // Recording
    const val microphonePermissionRequired = "Microphone permission required for voice messages."
    const val recordingFailed = "Could not start voice recording."
    const val cancelRecording = "Cancel recording"
    const val sendRecording = "Send voice message"
    const val recordVoice = "Record voice message"

    // Playback
    const val play = "Play"
    const val pause = "Pause"
    const val playbackSpeed = "Playback speed"
    const val playbackFailed = "Could not play voice message."

    // Upload
    const val presignIncomplete = "Media presign response is incomplete."
    const val confirmMissingUrl = "Media confirm response has no URL."
    const val downloadMissingUrl = "Media download URL is unavailable."
    const val fileNotFound = "Selected file was not found."
    const val fileTooLarge = "Files must be 10 MB or smaller."

    // Picker & viewer
    const val attachTitle = "Share attachment"
    const val photoFromGallery = "Photo from gallery"
    const val documentOrFile = "Document or file"
    const val previewAttachment = "Send attachment"
    const val addCaptionHint = "Add a caption (optional)"
    const val sendAttachment = "Send"
    const val changeFile = "Choose another"
    const val tapToDownload = "Tap to open"
    const val uploading = "Uploading..."
    const val loadingLink = "Loading..."
}
