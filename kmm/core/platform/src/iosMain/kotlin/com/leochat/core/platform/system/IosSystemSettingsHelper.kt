package com.leochat.core.platform.system

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosSystemSettingsHelper : SystemSettingsHelper {
    override val supportsOverlayPermission: Boolean = false
    override fun canDrawOverlays(): Boolean = false
    override fun areNotificationsEnabled(): Boolean = true
    override fun openOverlaySettings() = openNotificationSettings()
    override fun openNotificationSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}
