package com.leochat.features.chat.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.ContactsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.features.chat.presentation.controllers.SharedMediaController
import com.leochat.features.chat.presentation.states.SharedMediaFilter
import com.leochat.features.chat.presentation.widgets.ContactsCenteredPane
import com.leochat.features.chat.presentation.widgets.ContactsEmptyState
import com.leochat.features.chat.presentation.widgets.ContactsLoader
import com.leochat.features.chat.presentation.widgets.ContactsTopBar
import com.leochat.features.chat.presentation.widgets.SharedFilesList
import com.leochat.features.chat.presentation.widgets.SharedLinksList
import com.leochat.features.chat.presentation.widgets.SharedMediaFilterPills
import com.leochat.features.chat.presentation.widgets.SharedMediaGrid
import org.koin.compose.viewmodel.koinViewModel

/**
 * Media, files, and links shared in one conversation. Mirrors `SharedMediaPage`.
 *
 * @param conversationId the conversation whose messages are classified.
 * @param onBack pops the screen.
 */
@Composable
fun SharedMediaPage(
    conversationId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    controller: SharedMediaController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    LaunchedEffect(conversationId) { controller.load(conversationId) }
    val buckets = state.buckets

    Box(modifier.fillMaxSize().background(LeoColors.background)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ContactsTopBar(onBack = onBack) {
                Column {
                    Text(
                        ContactsStrings.sharedIn(state.title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        color = LeoColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(ContactsStrings.everythingInOnePlace, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = LeoColors.textSecondary)
                }
            }
            SharedMediaFilterPills(
                selected = state.filter,
                mediaCount = buckets.media.size,
                filesCount = buckets.files.size,
                linksCount = buckets.links.size,
                onSelect = controller::setFilter,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            Box(Modifier.weight(1f)) {
                val error = state.errorMessage
                when {
                    state.isLoading && state.messages.isEmpty() -> ContactsCenteredPane { ContactsLoader() }
                    error != null && state.messages.isEmpty() -> ContactsCenteredPane {
                        ContactsEmptyState(Icons.Rounded.ErrorOutline, error, "", iconColor = LeoColors.destructive)
                    }
                    else -> when (state.filter) {
                        SharedMediaFilter.Media -> SharedMediaGrid(buckets.media)
                        SharedMediaFilter.Files -> SharedFilesList(buckets.files)
                        SharedMediaFilter.Links -> SharedLinksList(buckets.links)
                    }
                }
            }
        }
    }
}
