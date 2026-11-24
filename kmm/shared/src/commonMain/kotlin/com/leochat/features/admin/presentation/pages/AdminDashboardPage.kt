package com.leochat.features.admin.presentation.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AdminStrings
import com.leochat.core.responsive.LeoLayout
import com.leochat.core.responsive.LocalLeoLayout
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.admin.presentation.controllers.AdminDashboardController
import com.leochat.features.admin.presentation.states.AdminDashboardState
import com.leochat.features.admin.presentation.widgets.AdminContentFrame
import com.leochat.features.admin.presentation.widgets.AdminErrorState
import com.leochat.features.admin.presentation.widgets.AdminForbiddenState
import com.leochat.features.admin.presentation.widgets.AdminLoading
import com.leochat.features.admin.presentation.widgets.AdminModulesSection
import com.leochat.features.admin.presentation.widgets.AdminStatsGrid
import com.leochat.features.admin.presentation.widgets.AdminTopBar
import com.leochat.features.admin.presentation.widgets.AdminWelcomeBanner
import com.leochat.features.admin.presentation.widgets.CollectAdminEffects
import org.koin.compose.viewmodel.koinViewModel

/**
 * Admin console home (`/admin`). Mirrors `AdminDashboardPage`.
 * [onConversations] matches Flutter's Conversations card, which opens `/app`.
 */
@Composable
fun AdminDashboardPage(
    onBack: () -> Unit,
    onUsers: () -> Unit,
    onReports: () -> Unit,
    onConversations: () -> Unit,
    controller: AdminDashboardController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAdminEffects(controller.effects)
    AdminDashboardContent(
        state = state,
        onBack = onBack,
        onRefresh = controller::refresh,
        onUsers = onUsers,
        onReports = onReports,
        onConversations = onConversations,
    )
}

@Composable
fun AdminDashboardContent(
    state: AdminDashboardState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onUsers: () -> Unit,
    onReports: () -> Unit,
    onConversations: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    LeoScreen(background = palette.background) {
        Column(Modifier.fillMaxSize()) {
            AdminTopBar(title = AdminStrings.adminConsole, onBack = onBack) {
                if (!state.isForbidden) {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = AdminStrings.refreshStats, tint = palette.textSecondary)
                    }
                }
            }
            if (state.isForbidden) {
                AdminForbiddenState(onBack = onBack)
            } else {
                AdminContentFrame {
                    DashboardBody(state, onRefresh, onUsers, onReports, onConversations)
                }
            }
        }
    }
}

@Composable
private fun DashboardBody(
    state: AdminDashboardState,
    onRefresh: () -> Unit,
    onUsers: () -> Unit,
    onReports: () -> Unit,
    onConversations: () -> Unit,
) {
    val wide = LocalLeoLayout.current != LeoLayout.Mobile
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        AdminWelcomeBanner()
        Spacer(Modifier.height(20.dp))
        val stats = state.stats
        when {
            stats != null -> AdminStatsGrid(stats, wide, onUsers, onConversations, onReports)
            state.isLoading -> AdminLoading(padding = 32.dp)
            state.errorMessage != null -> AdminErrorState(AdminStrings.statsLoadFailedPrefix + state.errorMessage, onRefresh)
        }
        Spacer(Modifier.height(24.dp))
        AdminModulesSection(onUsers = onUsers, onReports = onReports)
    }
}
