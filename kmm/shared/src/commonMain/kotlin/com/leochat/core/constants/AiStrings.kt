package com.leochat.core.constants

/** User-facing strings for the Leo AI tab. Mirrors the literals in `lib/features/ai`. */
object AiStrings {
    // Header and model picker
    const val clearHistory = "Clear history"
    const val selectModelTitle = "Select Intelligence Model"
    const val selectModelSubtitle = "Switch between specialized reasoning engines"

    // Delete dialog
    const val deleteTitle = "Delete AI Conversation"
    const val deleteBody = "Permanently delete this AI conversation and all messages on the server?"
    const val cancel = "Cancel"
    const val delete = "Delete"

    // Capabilities
    const val reasoning = "Reasoning"
    const val webSearch = "Web search"

    // Pending action banner
    const val actionConfirmationRequired = "Action Confirmation Required"
    fun actionRequested(action: String) = "AI requested to perform: $action"
    const val dismiss = "Dismiss"
    const val confirmAndRun = "Confirm & Run"

    // Empty state
    const val emptyTitle = "Leo AI Intelligence"
    const val emptySubtitle = "Ask complex architectural queries, generate code, or analyze system designs."

    // Generating indicator
    const val generating = "Leo AI is thinking and formulating response..."

    // Composer
    const val askAnything = "Ask Leo AI anything..."

    // Message item
    const val assistantName = "Leo AI"
    const val defaultModelId = "leo-ultra"
    const val copiedResponse = "Copied response to clipboard"

    // Thinking box
    fun thinkingProcess(durationLabel: String) = "Thinking Process $durationLabel"

    // Controller-generated assistant messages (rendered as plain text, as in Flutter)
    const val conversationUnavailable = "⚠️ **Server error:** Could not establish AI conversation."
    fun serverError(message: String) = "⚠️ **Server error:** $message"
    fun actionCompleted(action: String, message: String, data: String) = "✅ **$action**: $message\n`$data`"
    const val defaultActionName = "Action"
    const val defaultActionMessage = "Completed"

    // Prompt suggestions shown while the history is short
    val suggestionPrompts = listOf(
        "Explain STOMP sequence deduplication in chat architecture",
        "Write a Flutter widget for smooth real-time voice message player",
        "How does single-flight token refresh interceptor prevent 401 storms?",
        "Design an end-to-end database schema for a high scale chat app",
    )
}
