package com.leochat.features.ai.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette
import com.leochat.features.ai.domain.entities.AiModelInfo
import com.leochat.features.ai.presentation.controllers.AiController
import com.leochat.features.ai.presentation.states.AiState
import com.leochat.features.ai.presentation.widgets.AiCapabilityBar
import com.leochat.features.ai.presentation.widgets.AiComposer
import com.leochat.features.ai.presentation.widgets.AiDeleteDialog
import com.leochat.features.ai.presentation.widgets.AiEmptyState
import com.leochat.features.ai.presentation.widgets.AiGeneratingIndicator
import com.leochat.features.ai.presentation.widgets.AiMessageItem
import com.leochat.features.ai.presentation.widgets.AiModelPickerSheet
import com.leochat.features.ai.presentation.widgets.AiPendingActionBanner
import com.leochat.features.ai.presentation.widgets.AiSuggestionChips
import com.leochat.features.ai.presentation.widgets.AiTopBar
import org.koin.compose.viewmodel.koinViewModel

/**
 * Leo AI tab. Mirrors `AiPage`: model picker, capability toggles, action confirmation,
 * message list with generating indicator, prompt suggestions, and composer.
 * Embedded as-is in the shell on mobile (above the bottom nav) and tablet/desktop (beside the rail).
 */
@Composable
fun AiPage(
    modifier: Modifier = Modifier,
    controller: AiController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    AiPageContent(
        state = state,
        onSend = { controller.sendMessage(it) },
        onSelectModel = controller::selectModel,
        onToggleDeepThink = controller::toggleDeepThink,
        onToggleWebSearch = controller::toggleWebSearch,
        onDeleteConversation = { controller.deleteConversation() },
        onConfirmAction = { controller.confirmPendingAction() },
        onCancelAction = controller::cancelPendingAction,
        modifier = modifier,
    )
}

/** Stateless body, driven entirely by [state] and callbacks. */
@Composable
fun AiPageContent(
    state: AiState,
    onSend: (String) -> Unit,
    onSelectModel: (AiModelInfo) -> Unit,
    onToggleDeepThink: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onDeleteConversation: () -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalLeoPalette.current.isDark
    var input by rememberSaveable { mutableStateOf("") }
    var showModelPicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val itemCount = state.messages.size + if (state.isGenerating) 1 else 0

    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    fun send(preset: String? = null) {
        val text = preset ?: input
        if (text.isBlank()) return
        onSend(text)
        input = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) LeoColors.darkBackground else LeoColors.background)
            .imePadding(),
    ) {
        AiTopBar(
            selectedModel = state.selectedModel,
            onOpenModelPicker = { showModelPicker = true },
            onClearHistory = { showDeleteDialog = true },
        )
        AiCapabilityBar(
            isDeepThinkEnabled = state.isDeepThinkEnabled,
            isWebSearchEnabled = state.isWebSearchEnabled,
            onToggleDeepThink = onToggleDeepThink,
            onToggleWebSearch = onToggleWebSearch,
        )
        state.pendingAction?.let { pending ->
            AiPendingActionBanner(action = pending.action, onDismiss = onCancelAction, onConfirm = onConfirmAction)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (state.isEmpty) {
                AiEmptyState()
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(state.messages, key = { index, m -> "${m.id}-$index" }) { _, message ->
                        AiMessageItem(message)
                    }
                    if (state.isGenerating) item(key = GENERATING_KEY) { AiGeneratingIndicator() }
                }
            }
        }
        if (state.showSuggestions) {
            AiSuggestionChips(onSelect = { send(it) })
        }
        AiComposer(
            text = input,
            onTextChange = { input = it },
            onSend = { send() },
            isSendEnabled = !state.isGenerating,
        )
    }

    if (showModelPicker) {
        AiModelPickerSheet(
            currentModel = state.selectedModel,
            onSelect = {
                onSelectModel(it)
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false },
        )
    }
    if (showDeleteDialog) {
        AiDeleteDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteConversation()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

private const val GENERATING_KEY = "ai_generating"
