package com.leochat.feature.ai.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.leochat.core.designsystem.component.ai.AiCapabilityBar
import com.leochat.core.designsystem.component.ai.AiComposer
import com.leochat.core.designsystem.component.ai.AiDeleteDialog
import com.leochat.core.designsystem.component.ai.AiEmptyState
import com.leochat.core.designsystem.component.ai.AiGeneratingIndicator
import com.leochat.core.designsystem.component.ai.AiMessageItem
import com.leochat.core.designsystem.component.ai.AiModelPickerSheet
import com.leochat.core.designsystem.component.ai.AiPendingActionBanner
import com.leochat.core.designsystem.component.ai.AiSuggestionChips
import com.leochat.core.designsystem.component.ai.AiTopBar
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette
import com.leochat.feature.ai.domain.model.AiModelInfo
import com.leochat.feature.ai.presentation.mapper.findById
import com.leochat.feature.ai.presentation.mapper.toUi

/** Stateless body, driven entirely by [state] and callbacks with active asking animations. */
@Composable
fun AiContent(
    state: AiUiState,
    onSend: (String) -> Unit,
    onSelectModel: (AiModelInfo) -> Unit,
    onToggleDeepThink: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onDeleteConversation: () -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalLeoPalette.current
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
            .background(palette.background)
            .imePadding(),
    ) {
        AiTopBar(
            selectedModel = state.selectedModel.toUi(),
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
                AiEmptyState(onSelectPrompt = { send(it) })
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(state.messages, key = { index, m -> "${m.id}-$index" }) { _, message ->
                        Box(Modifier.animateItem()) {
                            AiMessageItem(message.toUi())
                        }
                    }
                    if (state.isGenerating) {
                        item(key = GENERATING_KEY) {
                            Box(Modifier.animateItem()) {
                                AiGeneratingIndicator()
                            }
                        }
                    }
                }
            }
        }

        // Active asking glow beam above composer
        AnimatedVisibility(
            visible = state.isGenerating,
            enter = fadeIn(tween(250)) + expandVertically(tween(250)),
            exit = fadeOut(tween(250)) + shrinkVertically(tween(250)),
        ) {
            val beamTransition = rememberInfiniteTransition(label = "ai_generating_beam")
            val beamProgress by beamTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "beam_progress",
            )
            val beamBrush = Brush.linearGradient(
                colors = listOf(
                    AiTokens.gradientStart,
                    AiTokens.gradientEnd,
                    LeoColors.primaryLight,
                    AiTokens.gradientStart,
                ),
                start = Offset(beamProgress - 500f, 0f),
                end = Offset(beamProgress + 500f, 0f),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(beamBrush),
            )
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
        val models = remember { AiModelInfo.models.map { it.toUi() } }
        AiModelPickerSheet(
            currentModelId = state.selectedModel.id,
            models = models,
            onSelect = { picked ->
                AiModelInfo.models.findById(picked.id)?.let(onSelectModel)
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
