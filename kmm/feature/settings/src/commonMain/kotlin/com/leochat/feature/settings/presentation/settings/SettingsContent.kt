package com.leochat.feature.settings.presentation.settings

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
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.component.settings.SettingsCard
import com.leochat.core.designsystem.component.settings.SettingsDivider
import com.leochat.core.designsystem.component.settings.SettingsFooterLinks
import com.leochat.core.designsystem.component.settings.SettingsHeader
import com.leochat.core.designsystem.component.settings.SettingsNavigationTile
import com.leochat.core.designsystem.component.settings.SettingsProfileCard
import com.leochat.core.designsystem.component.settings.SettingsSectionHeader
import com.leochat.core.designsystem.component.settings.SettingsSwitchTile
import com.leochat.core.designsystem.component.settings.SettingsTextScope
import com.leochat.core.designsystem.component.settings.SettingsVersionFooter
import com.leochat.core.designsystem.theme.LeoThemeTokens

/** Stateless settings home layout driven by [state]. */
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onSearch: () -> Unit,
    onEditProfile: () -> Unit,
    onToggleMessageNotifications: (Boolean) -> Unit,
    onToggleMessagePreviews: (Boolean) -> Unit,
    onToggleCallNotifications: (Boolean) -> Unit,
    onToggleOnlineStatus: (Boolean) -> Unit,
    onProfileVisibility: () -> Unit,
    onBlockedPeople: () -> Unit,
    onSecurity: () -> Unit,
    onSessions: () -> Unit,
    onAppearance: () -> Unit,
    onHelp: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    onToggleMessageNotifications,
                    SettingsStrings.messageNotificationsBody,
                )
                SettingsDivider()
                SettingsSwitchTile(
                    SettingsStrings.messagePreviews,
                    prefs.messagePreviews,
                    onToggleMessagePreviews,
                    SettingsStrings.messagePreviewsBody,
                )
                SettingsDivider()
                SettingsSwitchTile(
                    SettingsStrings.callNotifications,
                    prefs.callNotifications,
                    onToggleCallNotifications,
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
                    onToggleOnlineStatus,
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

            Spacer(Modifier.height(28.dp))

            SettingsFooterLinks(onHelp = onHelp, onSignOut = onSignOut, signingOut = state.isSigningOut)
            Spacer(Modifier.height(20.dp))
            SettingsVersionFooter()
        }
    }
}
