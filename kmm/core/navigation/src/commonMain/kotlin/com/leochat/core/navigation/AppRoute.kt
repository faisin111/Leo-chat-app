package com.leochat.core.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation routes. Mirrors the go_router paths in `app/app.dart`. */
object AppRoutes {
    @Serializable data object Splash          // /splash
    @Serializable data object Onboarding      // /onboarding
    @Serializable data object Login           // /login
    @Serializable data object Register        // /register
    @Serializable data object ForgotPassword  // /forgot-password
    @Serializable data object ResetPassword   // /reset-password
    @Serializable data object Home            // /app
    @Serializable data object Conversation    // full-screen conversation on mobile
    @Serializable data object NewConversation // /app/new-chat
    @Serializable data object NewGroup        // /app/new-group
    @Serializable data class GroupDetails(val conversationId: String) // /app/group/:id
    @Serializable data class SharedMedia(val conversationId: String)  // /app/media/:id
    @Serializable data object Call            // /call
    @Serializable data object EditProfile     // /settings/profile
    @Serializable data object Security        // /settings/security
    @Serializable data object Sessions        // /settings/sessions
    @Serializable data object Appearance      // /settings/appearance
}
