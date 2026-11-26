package com.leochat.android.push

import com.leochat.core.common.constants.AppStrings
import com.leochat.feature.settings.domain.model.SettingsPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PushNotificationPolicyTest {
    private val sampleMessage = PushEvent.NewMessage(
        messageId = "m1",
        conversationId = "c1",
        senderId = "u2",
        senderName = "Maya Chen",
        senderAvatarUrl = null,
        conversationTitle = null,
        messageType = "TEXT",
        displayTitle = "Maya Chen",
        displayBody = "Secret text",
    )

    private val sampleReaction = PushEvent.MessageReaction(
        messageId = "m1",
        conversationId = "c1",
        senderId = "u2",
        senderName = "Maya Chen",
        emoji = "🔥",
        messageType = "TEXT",
        displayTitle = "Maya Chen",
        displayBody = "Reacted 🔥 to: Secret text",
    )

    @Test
    fun `masks sender and body when messagePreviews is disabled`() {
        val prefs = NotificationPreferencesSnapshot.fromSettings(
            SettingsPreferences(messageNotifications = true, messagePreviews = false),
        )
        val decision = PushNotificationPolicy.resolveMessage(sampleMessage, viewingSameChat = false, prefs = prefs)
        assertEquals(MessageDispatchDecision.Show(AppStrings.appName, AppStrings.newMessageFallback), decision)

        val reactionDecision = PushNotificationPolicy.resolveReaction(sampleReaction, viewingSameChat = false, prefs = prefs)
        assertEquals(MessageDispatchDecision.Show(AppStrings.appName, AppStrings.reactedToMessage), reactionDecision)
    }

    @Test
    fun `shows full title and body when messagePreviews is enabled`() {
        val prefs = NotificationPreferencesSnapshot.fromSettings(
            SettingsPreferences(messageNotifications = true, messagePreviews = true),
        )
        val decision = PushNotificationPolicy.resolveMessage(sampleMessage, viewingSameChat = false, prefs = prefs)
        assertEquals(MessageDispatchDecision.Show("Maya Chen", "Secret text"), decision)
    }

    @Test
    fun `suppresses message alerts when messageNotifications is off or chat is active`() {
        val disabled = NotificationPreferencesSnapshot(messageNotifications = false, messagePreviews = true)
        assertEquals(
            MessageDispatchDecision.Suppress,
            PushNotificationPolicy.resolveMessage(sampleMessage, viewingSameChat = false, prefs = disabled),
        )
        assertEquals(
            MessageDispatchDecision.Suppress,
            PushNotificationPolicy.resolveReaction(sampleReaction, viewingSameChat = false, prefs = disabled),
        )

        val enabled = NotificationPreferencesSnapshot(messageNotifications = true, messagePreviews = true)
        assertEquals(
            MessageDispatchDecision.Suppress,
            PushNotificationPolicy.resolveMessage(sampleMessage, viewingSameChat = true, prefs = enabled),
        )
    }

    @Test
    fun `respects callNotifications preference`() {
        assertTrue(PushNotificationPolicy.shouldShowCallNotification(NotificationPreferencesSnapshot(callNotifications = true)))
        assertFalse(PushNotificationPolicy.shouldShowCallNotification(NotificationPreferencesSnapshot(callNotifications = false)))
    }
}
