package com.leochat.core.platform.system

/** Platform bridge for inspecting notification/overlay permissions and opening system settings. */
interface SystemSettingsHelper {
    val supportsOverlayPermission: Boolean
    fun canDrawOverlays(): Boolean
    fun areNotificationsEnabled(): Boolean
    fun openOverlaySettings()
    fun openNotificationSettings()
}

/** Fallback helper for tests and previews where no OS window context exists. */
object NoopSystemSettingsHelper : SystemSettingsHelper {
    override val supportsOverlayPermission: Boolean = false
    override fun canDrawOverlays(): Boolean = true
    override fun areNotificationsEnabled(): Boolean = true
    override fun openOverlaySettings() = Unit
    override fun openNotificationSettings() = Unit
}
