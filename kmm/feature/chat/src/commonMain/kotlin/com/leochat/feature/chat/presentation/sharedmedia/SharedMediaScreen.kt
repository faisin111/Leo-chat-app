package com.leochat.feature.chat.presentation.sharedmedia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * Media, files, and links shared in one conversation. Mirrors `SharedMediaScreen`.
 *
 * @param conversationId the conversation whose messages are classified.
 * @param onBack pops the screen.
 */
@Composable
fun SharedMediaScreen(
    conversationId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    controller: SharedMediaViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    LaunchedEffect(conversationId) { controller.load(conversationId) }
    SharedMediaContent(state = state, onBack = onBack, onSelectFilter = controller::setFilter, modifier = modifier)
}
