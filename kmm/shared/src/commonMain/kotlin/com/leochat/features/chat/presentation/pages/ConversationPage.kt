package com.leochat.features.chat.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.responsive.LeoLayout
import com.leochat.core.responsive.LocalLeoLayout
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.chat.presentation.controllers.ChatController

/** Full-screen conversation on mobile. Mirrors `_ConversationPage`. */
@Composable
fun ConversationPage(controller: ChatController, onBack: () -> Unit) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    val layout = LocalLeoLayout.current
    // Leaving the conversation (or resizing to tablet) returns to the shell's split view.
    LaunchedEffect(state.selectedConversation == null, layout) {
        if (state.selectedConversation == null || layout != LeoLayout.Mobile) onBack()
    }
    Box(Modifier.fillMaxSize().background(LeoColors.background).safeDrawingPadding()) {
        DetailPane(state, controller, showBackButton = true, onBack = onBack, onComingSoon = { snackBar.showInfo(it) })
    }
}
