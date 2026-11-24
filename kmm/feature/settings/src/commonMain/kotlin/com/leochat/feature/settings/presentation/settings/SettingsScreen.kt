package com.leochat.feature.settings.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.settings.presentation.CollectSettingsEffects
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings tab content. Mirrors `SettingsScreen`. Embedded by the chat shell on every layout
 * (mobile tab body; tablet/desktop main pane next to the rail), so it applies no window insets.
 * Sign-out matches Flutter: no confirmation; [onSignedOut] fires after logout completes.
 */
@Composable
fun SettingsScreen(
    onEditProfile: () -> Unit,
    onSecurity: () -> Unit,
    onSessions: () -> Unit,
    onAppearance: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onProfileVisibility: () -> Unit = {},
    onBlockedPeople: () -> Unit = {},
    onHelp: () -> Unit = {},
    controller: SettingsViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onSignedOut = onSignedOut)
    LaunchedEffect(controller) { controller.onVisible() }

    SettingsContent(
        state = state,
        modifier = modifier,
        onSearch = onSearch,
        onEditProfile = onEditProfile,
        onToggleMessageNotifications = controller::toggleMessageNotifications,
        onToggleMessagePreviews = controller::toggleMessagePreviews,
        onToggleCallNotifications = controller::toggleCallNotifications,
        onToggleOnlineStatus = controller::toggleOnlineStatus,
        onProfileVisibility = onProfileVisibility,
        onBlockedPeople = onBlockedPeople,
        onSecurity = onSecurity,
        onSessions = onSessions,
        onAppearance = onAppearance,
        onHelp = onHelp,
        onSignOut = controller::signOut,
    )
}
