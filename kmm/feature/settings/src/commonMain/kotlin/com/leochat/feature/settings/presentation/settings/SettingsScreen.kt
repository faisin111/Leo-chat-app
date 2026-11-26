package com.leochat.feature.settings.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.settings.presentation.CollectSettingsEffects
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings tab content with real search, blocked users, profile visibility sheet,
 * device permission diagnostics, and help sheet.
 */
@Composable
fun SettingsScreen(
    onEditProfile: () -> Unit,
    onBlockedPeople: () -> Unit,
    onSecurity: () -> Unit,
    onSessions: () -> Unit,
    onAppearance: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    controller: SettingsViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onSignedOut = onSignedOut)
    LaunchedEffect(controller) { controller.onVisible() }
    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                controller.refreshSystemPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SettingsContent(
        state = state,
        modifier = modifier,
        onToggleSearch = controller::toggleSearch,
        onSearchQueryChange = controller::onSearchQueryChange,
        onEditProfile = onEditProfile,
        onToggleMessageNotifications = controller::toggleMessageNotifications,
        onToggleMessagePreviews = controller::toggleMessagePreviews,
        onToggleCallNotifications = controller::toggleCallNotifications,
        onToggleOnlineStatus = controller::toggleOnlineStatus,
        onProfileVisibility = controller::openVisibilitySheet,
        onSelectProfileVisibility = controller::setProfileVisibility,
        onDismissVisibilitySheet = controller::dismissVisibilitySheet,
        onBlockedPeople = onBlockedPeople,
        onSecurity = onSecurity,
        onSessions = onSessions,
        onAppearance = onAppearance,
        onHelp = controller::openHelpSheet,
        onDismissHelpSheet = controller::dismissHelpSheet,
        onOpenOverlaySettings = controller::openOverlaySettings,
        onOpenNotificationSettings = controller::openNotificationSettings,
        onSignOut = controller::signOut,
    )
}
