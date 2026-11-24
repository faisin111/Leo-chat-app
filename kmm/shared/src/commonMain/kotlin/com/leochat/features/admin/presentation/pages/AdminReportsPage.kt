package com.leochat.features.admin.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AdminStrings
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.admin.domain.entities.AdminReport
import com.leochat.features.admin.presentation.controllers.AdminReportsController
import com.leochat.features.admin.presentation.states.AdminReportsState
import com.leochat.features.admin.presentation.widgets.AdminCenteredMessage
import com.leochat.features.admin.presentation.widgets.AdminContentFrame
import com.leochat.features.admin.presentation.widgets.AdminErrorState
import com.leochat.features.admin.presentation.widgets.AdminFilterPills
import com.leochat.features.admin.presentation.widgets.AdminForbiddenState
import com.leochat.features.admin.presentation.widgets.AdminLoading
import com.leochat.features.admin.presentation.widgets.AdminReportCard
import com.leochat.features.admin.presentation.widgets.AdminReportDialog
import com.leochat.features.admin.presentation.widgets.AdminTopBar
import com.leochat.features.admin.presentation.widgets.CollectAdminEffects
import org.koin.compose.viewmodel.koinViewModel

/** Report moderation (`/admin/reports`). Mirrors `AdminReportsPage`. */
@Composable
fun AdminReportsPage(
    onBack: () -> Unit,
    controller: AdminReportsController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAdminEffects(controller.effects)
    AdminReportsContent(
        state = state,
        onBack = onBack,
        onSelectFilter = controller::selectFilter,
        onRetry = controller::retry,
        onResolve = controller::resolve,
        onDeleteMessage = controller::deleteMessage,
    )
}

@Composable
fun AdminReportsContent(
    state: AdminReportsState,
    onBack: () -> Unit,
    onSelectFilter: (String) -> Unit,
    onRetry: () -> Unit,
    onResolve: (AdminReport, String) -> Boolean,
    onDeleteMessage: (AdminReport) -> Unit,
) {
    var dialogReport by remember { mutableStateOf<AdminReport?>(null) }
    LeoScreen(background = LeoThemeTokens.palette.background) {
        Column(Modifier.fillMaxSize()) {
            AdminTopBar(title = AdminStrings.reportModeration, onBack = onBack)
            if (state.isForbidden) {
                AdminForbiddenState(onBack = onBack)
            } else {
                AdminContentFrame {
                    Column(Modifier.fillMaxSize()) {
                        AdminFilterPills(
                            filters = state.filters,
                            selected = state.filter,
                            onSelect = onSelectFilter,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )
                        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                            ReportsList(state, onRetry, onOpen = { dialogReport = it })
                        }
                    }
                }
            }
        }
    }
    dialogReport?.let { report ->
        AdminReportDialog(
            report = report,
            onDismiss = { dialogReport = null },
            onDeleteMessage = { onDeleteMessage(report) },
            onResolve = { note -> onResolve(report, note) },
        )
    }
}

@Composable
private fun ReportsList(state: AdminReportsState, onRetry: () -> Unit, onOpen: (AdminReport) -> Unit) {
    when {
        state.isLoading -> AdminLoading()
        state.errorMessage != null -> AdminErrorState(AdminStrings.reportsLoadFailedPrefix + state.errorMessage, onRetry)
        state.reports.isEmpty() -> AdminCenteredMessage(AdminStrings.noReportsFound)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.reports) { report ->
                AdminReportCard(report = report, onClick = { onOpen(report) })
            }
        }
    }
}
