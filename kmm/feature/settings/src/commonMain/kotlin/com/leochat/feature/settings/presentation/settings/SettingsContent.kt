package com.leochat.feature.settings.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
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
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

/** Stateless settings home layout driven by [state]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditProfile: () -> Unit,
    onToggleMessageNotifications: (Boolean) -> Unit,
    onToggleMessagePreviews: (Boolean) -> Unit,
    onToggleCallNotifications: (Boolean) -> Unit,
    onToggleOnlineStatus: (Boolean) -> Unit,
    onProfileVisibility: () -> Unit,
    onSelectProfileVisibility: (String) -> Unit,
    onDismissVisibilitySheet: () -> Unit,
    onBlockedPeople: () -> Unit,
    onSecurity: () -> Unit,
    onSessions: () -> Unit,
    onAppearance: () -> Unit,
    onHelp: () -> Unit,
    onDismissHelpSheet: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs = state.preferences
    val palette = LeoThemeTokens.palette
    val query = state.searchQuery.trim()
    fun matches(vararg terms: String): Boolean =
        query.isEmpty() || terms.any { it.contains(query, ignoreCase = true) }

    val notificationItems = buildList<@Composable () -> Unit> {
        if (matches(SettingsStrings.messageNotifications, SettingsStrings.messageNotificationsBody, "push", "alerts")) {
            add {
                SettingsSwitchTile(
                    SettingsStrings.messageNotifications,
                    prefs.messageNotifications,
                    onToggleMessageNotifications,
                    SettingsStrings.messageNotificationsBody,
                )
            }
        }
        if (matches(SettingsStrings.messagePreviews, SettingsStrings.messagePreviewsBody, "privacy", "preview")) {
            add {
                SettingsSwitchTile(
                    SettingsStrings.messagePreviews,
                    prefs.messagePreviews,
                    onToggleMessagePreviews,
                    SettingsStrings.messagePreviewsBody,
                )
            }
        }
        if (matches(SettingsStrings.callNotifications, SettingsStrings.callNotificationsBody, "ring", "voice", "video")) {
            add {
                SettingsSwitchTile(
                    SettingsStrings.callNotifications,
                    prefs.callNotifications,
                    onToggleCallNotifications,
                    SettingsStrings.callNotificationsBody,
                )
            }
        }
        if (state.supportsOverlayPermission &&
            matches(SettingsStrings.floatingCallPopup, SettingsStrings.floatingCallPopupEnabled, SettingsStrings.floatingCallPopupDisabled, "overlay")
        ) {
            add {
                SettingsNavigationTile(
                    title = SettingsStrings.floatingCallPopup,
                    onClick = onOpenOverlaySettings,
                    trailingText = if (state.overlayPermissionGranted) SettingsStrings.helpEnabled else SettingsStrings.managePermission,
                )
            }
        }
        if (!state.osNotificationsEnabled &&
            matches(SettingsStrings.deviceNotifications, SettingsStrings.deviceNotificationsDisabled, "os")
        ) {
            add {
                SettingsNavigationTile(
                    title = SettingsStrings.deviceNotifications,
                    onClick = onOpenNotificationSettings,
                    trailingText = SettingsStrings.helpDisabled,
                )
            }
        }
    }

    val privacyItems = buildList<@Composable () -> Unit> {
        if (matches(SettingsStrings.profileVisibility, state.profileVisibility, "privacy")) {
            add {
                SettingsNavigationTile(SettingsStrings.profileVisibility, onProfileVisibility, state.profileVisibility)
            }
        }
        if (matches(SettingsStrings.onlineStatus, SettingsStrings.onlineStatusBody, "presence", "active")) {
            add {
                SettingsSwitchTile(
                    SettingsStrings.onlineStatus,
                    prefs.onlineStatus,
                    onToggleOnlineStatus,
                    SettingsStrings.onlineStatusBody,
                )
            }
        }
        if (matches(SettingsStrings.blockedPeople, "block")) {
            add {
                SettingsNavigationTile(SettingsStrings.blockedPeople, onBlockedPeople, state.blockedCount.toString())
            }
        }
        if (matches(SettingsStrings.passwordAndSecurity, "password", "deactivate", "2fa")) {
            add { SettingsNavigationTile(SettingsStrings.passwordAndSecurity, onSecurity) }
        }
        if (matches(SettingsStrings.sessionsAndDevices, "devices", "logout", "sessions")) {
            add {
                SettingsNavigationTile(SettingsStrings.sessionsAndDevices, onSessions, SettingsStrings.sessionsTrailing)
            }
        }
        if (matches(SettingsStrings.appearanceAndTheme, "dark", "light", "theme", "accent", "color")) {
            add {
                SettingsNavigationTile(SettingsStrings.appearanceAndTheme, onAppearance, SettingsStrings.appearanceTrailing)
            }
        }
    }

    SettingsTextScope {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(palette.background)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 32.dp),
        ) {
            SettingsHeader(onToggleSearch)
            AnimatedVisibility(visible = state.isSearching) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    SettingsSearchBar(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onClearOrClose = {
                            if (state.searchQuery.isNotEmpty()) onSearchQueryChange("") else onToggleSearch()
                        },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (query.isEmpty() || matches(state.displayName, state.username, "profile", "edit")) {
                SettingsProfileCard(state.displayName, state.username, state.avatarUrl, onEditProfile)
                Spacer(Modifier.height(24.dp))
            }

            if (notificationItems.isNotEmpty()) {
                SettingsSectionHeader(Icons.Rounded.NotificationsNone, SettingsStrings.notificationsSection)
                Spacer(Modifier.height(10.dp))
                SettingsCard {
                    notificationItems.forEachIndexed { index, item ->
                        item()
                        if (index != notificationItems.lastIndex) SettingsDivider()
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            if (privacyItems.isNotEmpty()) {
                SettingsSectionHeader(Icons.Outlined.Shield, SettingsStrings.privacySection)
                Spacer(Modifier.height(10.dp))
                SettingsCard {
                    privacyItems.forEachIndexed { index, item ->
                        item()
                        if (index != privacyItems.lastIndex) SettingsDivider()
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            if (notificationItems.isEmpty() && privacyItems.isEmpty() && query.isNotEmpty()) {
                SettingsCard(padding = 24.dp) {
                    Text(
                        SettingsStrings.noMatchingSettings,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textSecondary,
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            SettingsFooterLinks(onHelp = onHelp, onSignOut = onSignOut, signingOut = state.isSigningOut)
            Spacer(Modifier.height(20.dp))
            SettingsVersionFooter()
        }

        if (state.showVisibilitySheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissVisibilitySheet,
                containerColor = if (palette.isDark) palette.surfaceElevated else palette.surface,
            ) {
                VisibilitySheetContent(
                    selected = state.profileVisibility,
                    onSelect = onSelectProfileVisibility,
                )
            }
        }

        if (state.showHelpSheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissHelpSheet,
                containerColor = if (palette.isDark) palette.surfaceElevated else palette.surface,
            ) {
                HelpDiagnosticsSheetContent(
                    state = state,
                    onOpenNotificationSettings = {
                        onDismissHelpSheet()
                        onOpenNotificationSettings()
                    },
                    onOpenOverlaySettings = {
                        onDismissHelpSheet()
                        onOpenOverlaySettings()
                    },
                    onClose = onDismissHelpSheet,
                )
            }
        }
    }
}

@Composable
private fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearOrClose: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (palette.isDark) palette.surfaceElevated else palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(SettingsStrings.searchPlaceholder, fontSize = 14.sp, color = palette.textMuted)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.merge(
                    TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary),
                ),
                cursorBrush = SolidColor(LeoColors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        IconButton(onClick = onClearOrClose, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = SettingsStrings.close, tint = palette.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun VisibilitySheetContent(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val options = listOf(
        SettingsStrings.profileVisibilityEveryone,
        SettingsStrings.profileVisibilityContacts,
        SettingsStrings.profileVisibilityNobody,
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
    ) {
        Text(SettingsStrings.profileVisibilityTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(SettingsStrings.profileVisibilitySubtitle, fontSize = 13.sp, color = palette.textSecondary)
        Spacer(Modifier.height(16.dp))
        SettingsCard {
            options.forEachIndexed { index, option ->
                val isSelected = option.equals(selected, ignoreCase = true)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(option) }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Text(
                        option,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) LeoColors.primary else palette.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (isSelected) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(20.dp))
                    }
                }
                if (index != options.lastIndex) SettingsDivider()
            }
        }
    }
}

@Composable
private fun HelpDiagnosticsSheetContent(
    state: SettingsUiState,
    onOpenNotificationSettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onClose: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
    ) {
        Column {
            Text(SettingsStrings.helpTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(SettingsStrings.helpSubtitle, fontSize = 13.sp, color = palette.textSecondary)
        }
        SettingsCard(padding = 16.dp) {
            DiagnosticRow(SettingsStrings.helpAppVersion, "v1.0")
            Spacer(Modifier.height(10.dp))
            DiagnosticRow(
                SettingsStrings.helpPushStatus,
                if (state.osNotificationsEnabled) SettingsStrings.helpEnabled else SettingsStrings.helpDisabled,
            )
            if (state.supportsOverlayPermission) {
                Spacer(Modifier.height(10.dp))
                DiagnosticRow(
                    SettingsStrings.helpOverlayStatus,
                    if (state.overlayPermissionGranted) SettingsStrings.helpEnabled else SettingsStrings.helpDisabled,
                )
            }
        }
        Text(SettingsStrings.helpTroubleshootingTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
        Text(SettingsStrings.helpTroubleshootingBody, fontSize = 13.sp, color = palette.textSecondary)
        LeoPrimaryButton(
            label = SettingsStrings.helpOpenNotificationSettings,
            onClick = onOpenNotificationSettings,
            modifier = Modifier.fillMaxWidth().height(46.dp),
        )
        if (state.supportsOverlayPermission) {
            TextButton(onClick = onOpenOverlaySettings, modifier = Modifier.fillMaxWidth()) {
                Text(SettingsStrings.helpOpenOverlaySettings, color = LeoColors.primary, fontWeight = FontWeight.SemiBold)
            }
        }
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(SettingsStrings.close, color = palette.textSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    val palette = LeoThemeTokens.palette
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
    }
}
