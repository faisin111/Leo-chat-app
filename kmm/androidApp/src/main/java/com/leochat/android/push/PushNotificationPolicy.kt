package com.leochat.android.push

import com.leochat.core.common.constants.AppStrings
import com.leochat.feature.settings.domain.model.SettingsPreferences

internal data class NotificationPreferencesSnapshot(
    val messageNotifications: Boolean = true,
    val messagePreviews: Boolean = false,
    val callNotifications: Boolean = true,
) {
    companion object {
        fun fromSettings(prefs: SettingsPreferences?) =
            if (prefs == null) {
                NotificationPreferencesSnapshot()
            } else {
                NotificationPreferencesSnapshot(
                    messageNotifications = prefs.messageNotifications,
                    messagePreviews = prefs.messagePreviews,
                    callNotifications = prefs.callNotifications,
                )
            }
    }
}

internal sealed interface MessageDispatchDecision {
    data object Suppress : MessageDispatchDecision
    data class Show(val title: String, val body: String) : MessageDispatchDecision
}

/** Enforces Settings notification and preview toggles for incoming FCM alerts. */
internal object PushNotificationPolicy {
    fun resolveMessage(
        event: PushEvent.NewMessage,
        viewingSameChat: Boolean,
        prefs: NotificationPreferencesSnapshot,
    ): MessageDispatchDecision {
        if (viewingSameChat || !prefs.messageNotifications) return MessageDispatchDecision.Suppress
        val title = if (prefs.messagePreviews) event.displayTitle else AppStrings.appName
        val body = if (prefs.messagePreviews) event.displayBody else AppStrings.newMessageFallback
        return MessageDispatchDecision.Show(title = title, body = body)
    }

    fun resolveReaction(
        event: PushEvent.MessageReaction,
        viewingSameChat: Boolean,
        prefs: NotificationPreferencesSnapshot,
    ): MessageDispatchDecision {
        if (viewingSameChat || !prefs.messageNotifications) return MessageDispatchDecision.Suppress
        val title = if (prefs.messagePreviews) event.displayTitle else AppStrings.appName
        val body = if (prefs.messagePreviews) event.displayBody else AppStrings.reactedToMessage
        return MessageDispatchDecision.Show(title = title, body = body)
    }

    fun shouldShowCallNotification(prefs: NotificationPreferencesSnapshot): Boolean =
        prefs.callNotifications
}
