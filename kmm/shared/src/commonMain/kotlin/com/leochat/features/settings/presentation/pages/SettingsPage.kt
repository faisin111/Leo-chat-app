package com.leochat.features.settings.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.settings.presentation.controllers.SettingsController
import com.leochat.features.settings.presentation.widgets.CollectSettingsEffects
import com.leochat.features.settings.presentation.widgets.SettingsCard
import com.leochat.features.settings.presentation.widgets.SettingsDivider
import com.leochat.features.settings.presentation.widgets.SettingsFooterLinks
import com.leochat.features.settings.presentation.widgets.SettingsHeader
import com.leochat.features.settings.presentation.widgets.SettingsNavigationTile
import com.leochat.features.settings.presentation.widgets.SettingsProfileCard
import com.leochat.features.settings.presentation.widgets.SettingsSectionHeader
import com.leochat.features.settings.presentation.widgets.SettingsSwitchTile
import com.leochat.features.settings.presentation.widgets.SettingsTextScope
import com.leochat.features.settings.presentation.widgets.SettingsVersionFooter
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings tab content. Mirrors `SettingsPage`. Embedded by the chat shell on every layout
 * (mobile tab body; tablet/desktop main pane next to the rail), so it applies no window insets.
 * Sign-out matches Flutter: no confirmation; [onSignedOut] fires after logout completes.
 */
@Composable
fun SettingsPage(
    onEditProfile: () -> Unit,
    onSecurity: () -> Unit,
    onSessions: () -> Unit,
    onAppearance: () -> Unit,
    onAdmin: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onProfileVisibility: () -> Unit = {},
    onBlockedPeople: () -> Unit = {},
    onHelp: () -> Unit = {},
    controller: SettingsController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onSignedOut = onSignedOut)
    LaunchedEffect(controller) { controller.onVisible() }
    val prefs = state.preferences

    SettingsTextScope {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LeoThemeTokens.palette.background)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 32.dp),
        ) {
            SettingsHeader(onSearch)
            Spacer(Modifier.height(16.dp))
            SettingsProfileCard(state.displayName, state.username, state.avatarUrl, onEditProfile)
            Spacer(Modifier.height(24.dp))

            SettingsSectionHeader(Icons.Rounded.NotificationsNone, SettingsStrings.notificationsSection)
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                SettingsSwitchTile(
                    SettingsStrings.messageNotifications,
                    prefs.messageNotifications,
                    controller::toggleMessageNotifications,
                    SettingsStrings.messageNotificationsBody,
                )
                SettingsDivider()
                SettingsSwitchTile(
                    SettingsStrings.messagePreviews,
                    prefs.messagePreviews,
                    controller::toggleMessagePreviews,
                    SettingsStrings.messagePreviewsBody,
                )
                SettingsDivider()
                SettingsSwitchTile(
                    SettingsStrings.callNotifications,
                    prefs.callNotifications,
                    controller::toggleCallNotifications,
                    SettingsStrings.callNotificationsBody,
                )
            }
            Spacer(Modifier.height(24.dp))

            SettingsSectionHeader(Icons.Outlined.Shield, SettingsStrings.privacySection)
            Spacer(Modifier.height(10.dp))
            SettingsCard {
                SettingsNavigationTile(SettingsStrings.profileVisibility, onProfileVisibility, state.profileVisibility)
                SettingsDivider()
                SettingsSwitchTile(
                    SettingsStrings.onlineStatus,
                    prefs.onlineStatus,
                    controller::toggleOnlineStatus,
                    SettingsStrings.onlineStatusBody,
                )
                SettingsDivider()
                SettingsNavigationTile(SettingsStrings.blockedPeople, onBlockedPeople, state.blockedCount.toString())
                SettingsDivider()
                SettingsNavigationTile(SettingsStrings.passwordAndSecurity, onSecurity)
                SettingsDivider()
                SettingsNavigationTile(SettingsStrings.sessionsAndDevices, onSessions, SettingsStrings.sessionsTrailing)
                SettingsDivider()
                SettingsNavigationTile(SettingsStrings.appearanceAndTheme, onAppearance, SettingsStrings.appearanceTrailing)
            }

            if (state.isAdmin) {
                Spacer(Modifier.height(24.dp))
                SettingsSectionHeader(Icons.Rounded.AdminPanelSettings, SettingsStrings.adminSection)
                Spacer(Modifier.height(10.dp))
                SettingsCard {
                    SettingsNavigationTile(SettingsStrings.adminConsole, onAdmin, SettingsStrings.adminConsoleTrailing)
                }
            }
            Spacer(Modifier.height(28.dp))

            SettingsFooterLinks(onHelp = onHelp, onSignOut = controller::signOut, signingOut = state.isSigningOut)
            Spacer(Modifier.height(20.dp))
            SettingsVersionFooter()
        }
    }
}
