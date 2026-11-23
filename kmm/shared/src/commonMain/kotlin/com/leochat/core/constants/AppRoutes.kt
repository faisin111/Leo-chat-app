package com.leochat.core.constants

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
}
