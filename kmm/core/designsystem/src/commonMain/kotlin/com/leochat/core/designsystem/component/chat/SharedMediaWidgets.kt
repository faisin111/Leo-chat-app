package com.leochat.core.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.designsystem.component.chat.model.FilterOption
import com.leochat.core.designsystem.component.chat.model.SharedItemUi
import com.leochat.core.designsystem.component.chat.model.SharedMediaThumbUi
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.lineHeight

/** Segmented control with equal-width options (Media / Files / Links). */
@Composable
fun <T> SegmentedFilterPills(
    options: List<FilterOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    val trackColor = if (palette.isDark) palette.surfaceElevated else ChatTokens.filterTrack
    Row(modifier.fillMaxWidth().background(trackColor, RoundedCornerShape(16.dp)).padding(4.dp)) {
        options.forEach { option ->
            FilterPill(option.label, option.value == selected, { onSelect(option.value) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(12.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .contactsTap(onClick = onClick)
            .then(if (isSelected) Modifier.shadow(2.dp, shape, ambientColor = ChatTokens.pillShadow, spotColor = ChatTokens.pillShadow) else Modifier)
            .background(if (isSelected) palette.surface else Color.Transparent, shape)
            .padding(vertical = 8.dp),
    ) {
        Text(
            label,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isSelected) LeoColors.primary else palette.textSecondary,
            maxLines = 1,
        )
    }
}

/** Three-column square thumbnails. */
@Composable
fun SharedMediaGrid(items: List<SharedMediaThumbUi>) {
    if (items.isEmpty()) {
        SharedEmptyState(Icons.Outlined.PhotoLibrary, ContactsStrings.noMediaYet, ContactsStrings.noMediaYetHint)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { item ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(ChatTokens.imagePlaceholder),
            ) {
                if (item.isRemote) {
                    SubcomposeAsyncImage(
                        model = item.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.BrokenImage, null, tint = ChatTokens.imageIcon, modifier = Modifier.size(24.dp))
                            }
                        },
                    )
                } else {
                    Icon(Icons.Rounded.Image, null, tint = ChatTokens.imageIcon, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

/** Shared files: document tile, file name, message type. */
@Composable
fun SharedFilesList(items: List<SharedItemUi>) {
    val palette = LeoThemeTokens.palette
    if (items.isEmpty()) {
        SharedEmptyState(Icons.AutoMirrored.Outlined.InsertDriveFile, ContactsStrings.noFilesYet, ContactsStrings.noFilesYetHint)
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items, key = { _, m -> m.id }) { index, item ->
            if (index > 0) ContactsDivider(64.dp)
            SharedItemRow(
                icon = Icons.Rounded.Description,
                iconTint = LeoColors.info,
                tileColor = if (palette.isDark) palette.surfaceElevated else ChatTokens.fileTileBackground,
                title = item.title,
                titleColor = palette.textPrimary,
                titleSize = 15,
                subtitle = item.subtitle,
            )
        }
    }
}

/** Shared links: link tile, first URL, full message text. */
@Composable
fun SharedLinksList(items: List<SharedItemUi>) {
    val palette = LeoThemeTokens.palette
    if (items.isEmpty()) {
        SharedEmptyState(Icons.Rounded.Link, ContactsStrings.noLinksYet, ContactsStrings.noLinksYetHint)
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items, key = { _, m -> m.id }) { index, item ->
            if (index > 0) ContactsDivider(64.dp)
            SharedItemRow(
                icon = Icons.Rounded.Link,
                iconTint = LeoColors.primary,
                tileColor = palette.primarySoft,
                title = item.title,
                titleColor = LeoColors.primary,
                titleSize = 14,
                subtitle = item.subtitle,
            )
        }
    }
}

@Composable
private fun SharedItemRow(
    icon: ImageVector,
    iconTint: Color,
    tileColor: Color,
    title: String,
    titleColor: Color,
    titleSize: Int,
    subtitle: String,
) {
    val palette = LeoThemeTokens.palette
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Box(Modifier.size(44.dp).background(tileColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = titleSize.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = palette.textSecondary)
        }
    }
}

@Composable
private fun SharedEmptyState(icon: ImageVector, title: String, subtitle: String) {
    ContactsCenteredPane {
        ContactsEmptyState(icon, title, subtitle, discSize = 60.dp, subtitleLineHeight = lineHeight(13, 1.4))
    }
}
