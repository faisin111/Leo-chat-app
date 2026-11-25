package com.leochat.feature.chat.presentation.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.responsive.LeoLayout
import com.leochat.core.designsystem.responsive.LocalLeoLayout
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.navigation.DeepLinkBus
import com.leochat.feature.chat.presentation.ChatViewModel

/** Full-screen conversation on mobile. Mirrors `_ConversationPage`. */
@Composable
fun ConversationScreen(controller: ChatViewModel, nav: ConversationNavigator, onBack: () -> Unit) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    val layout = LocalLeoLayout.current
    val palette = LeoThemeTokens.palette
    val conversationId = state.selectedConversationId
    DisposableEffect(conversationId) {
        DeepLinkBus.setActiveConversation(conversationId)
        onDispose {
            if (DeepLinkBus.activeConversationId.value == conversationId) {
                DeepLinkBus.setActiveConversation(null)
            }
        }
    }
    // Leaving the conversation (or resizing to tablet) returns to the shell's split view.
    LaunchedEffect(state.selectedConversation == null, layout) {
        if (state.selectedConversation == null || layout != LeoLayout.Mobile) onBack()
    }
    Box(Modifier.fillMaxSize().background(palette.background).safeDrawingPadding()) {
        ConversationPane(state, controller, nav, showBackButton = true, onBack = onBack, onComingSoon = { snackBar.showInfo(it) })
    }
}
