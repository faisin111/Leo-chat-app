package com.leochat.feature.chat.presentation.sharedmedia

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.designsystem.component.chat.ContactsCenteredPane
import com.leochat.core.designsystem.component.chat.ContactsEmptyState
import com.leochat.core.designsystem.component.chat.ContactsLoader
import com.leochat.core.designsystem.component.chat.ContactsTopBar
import com.leochat.core.designsystem.component.chat.SegmentedFilterPills
import com.leochat.core.designsystem.component.chat.SharedFilesList
import com.leochat.core.designsystem.component.chat.SharedLinksList
import com.leochat.core.designsystem.component.chat.SharedMediaGrid
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.feature.chat.presentation.SharedMediaFilter
import com.leochat.feature.chat.presentation.SharedMediaUiState
import com.leochat.feature.chat.presentation.mapper.sharedMediaFilterOptions
import com.leochat.feature.chat.presentation.mapper.toSharedFileUi
import com.leochat.feature.chat.presentation.mapper.toSharedLinkUi
import com.leochat.feature.chat.presentation.mapper.toSharedThumbUi

/** Stateless shared media UI driven by [SharedMediaUiState]. */
@Composable
fun SharedMediaContent(
    state: SharedMediaUiState,
    onBack: () -> Unit,
    onSelectFilter: (SharedMediaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            SegmentedFilterPills(
                options = sharedMediaFilterOptions(buckets.media.size, buckets.files.size, buckets.links.size),
                selected = state.filter,
                onSelect = onSelectFilter,
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
                        SharedMediaFilter.Media -> SharedMediaGrid(buckets.media.map { it.toSharedThumbUi() })
                        SharedMediaFilter.Files -> SharedFilesList(buckets.files.map { it.toSharedFileUi() })
                        SharedMediaFilter.Links -> SharedLinksList(buckets.links.map { it.toSharedLinkUi() })
                    }
                }
            }
        }
    }
}
