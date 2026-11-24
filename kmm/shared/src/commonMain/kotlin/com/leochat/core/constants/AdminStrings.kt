package com.leochat.core.constants

/** User-facing copy for the admin console. Mirrors the literals in `lib/features/admin`. */
object AdminStrings {
    // Titles
    const val adminConsole = "Admin console"
    const val userManagement = "User management"
    const val reportModeration = "Report moderation"
    const val back = "Back"

    // Dashboard
    const val refreshStats = "Refresh stats"
    const val administratorBadge = "ADMINISTRATOR"
    const val systemsOperational = "Systems Operational"
    const val overviewTitle = "Overview & Moderation"
    const val overviewSubtitle = "Monitor registered accounts, active channels, and resolved user reports."
    const val totalUsers = "Total Users"
    const val conversations = "Conversations"
    const val reportsAndFlagged = "Reports & Flagged Content"
    const val managementModules = "MANAGEMENT MODULES"
    const val userManagementTile = "User Management"
    const val userManagementSubtitle = "Search, change status, and force logout"
    const val reportModerationTile = "Report Moderation"
    const val reportModerationSubtitle = "Review complaints and delete abusive messages"
    const val statsLoadFailedPrefix = "Failed to load stats: "

    // Users
    const val searchUsersHint = "Search by username or display name..."
    const val noUsersFound = "No users found matching query"
    const val usersLoadFailedPrefix = "Error loading users: "
    const val adminBadge = "ADMIN"
    const val markActive = "Mark as ACTIVE"
    const val markDisabled = "Mark as DISABLED"
    const val markBanned = "Mark as BANNED"
    const val forceLogout = "Force Logout from all devices"
    const val userMarkedActive = "User marked ACTIVE"
    const val userMarkedDisabled = "User status changed to DISABLED"
    const val userMarkedBanned = "User status changed to BANNED"
    const val forceLoggedOut = "Force logged out sessions"
    const val userActions = "User actions"

    // Reports
    const val noReportsFound = "No reports found in this category"
    const val reportsLoadFailedPrefix = "Error loading reports: "
    const val resolutionNote = "Resolution note"
    const val resolutionHint = "e.g. Warning issued or Verified no violation"
    const val resolutionRequired = "Add a resolution note before resolving."
    const val cancel = "Cancel"
    const val deleteMessage = "Delete Message"
    const val resolve = "Resolve"
    const val reportedMessageDeleted = "Reported message deleted"
    const val reportResolved = "Report resolved successfully"

    // Shared states
    const val retry = "Retry"
    const val forbiddenTitle = "Admins only"
    const val forbiddenBody = "You don't have permission to open the admin console. Ask an administrator for access."
    const val goBack = "Go back"

    private const val separator = " · "

    fun handleWithStatus(username: String, status: String) = "@$username$separator$status"
    fun handleWithEmail(username: String, email: String) = "@$username$separator$email"
    fun reportTitle(reason: String) = "Report: $reason"
    fun reportTarget(type: String, id: String) = "Target: $type ($id)"
    fun reportDetails(details: String) = "Details: $details"
    fun reportResolution(resolution: String) = "Resolution: $resolution"
    fun deletedByModerator(reason: String) = "Deleted by moderator for $reason"
}
