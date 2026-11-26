package com.leochat.feature.notifications.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.util.TimeFormat
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.notifications.domain.model.AppNotification
import com.leochat.feature.notifications.domain.model.NotificationType
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel

/** Persistent Notification Center screen. Mirrors LeoChat Web's `NotificationsPage`. */
@Composable
fun NotificationsScreen(
    onOpenConversation: (conversationId: String) -> Unit,
    modifier: Modifier = Modifier,
    controller: NotificationsViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current

    LaunchedEffect(state.error) {
        val message = state.error ?: return@LaunchedEffect
        snackBar.showError(message)
        controller.clearError()
    }

    NotificationsContent(
        state = state,
        onRefresh = { controller.refresh(showSpinner = true) },
        onSelectReadFilter = controller::setReadFilter,
        onSelectTypeFilter = controller::setTypeFilter,
        onMarkAllRead = controller::markAllRead,
        onMarkRead = controller::markRead,
        onDelete = controller::delete,
        onOpenNotification = { item -> controller.openNotification(item, onOpenConversation) },
        onLoadMore = controller::loadMore,
        onResetFilters = {
            controller.setReadFilter(NotificationsReadFilter.All)
            controller.setTypeFilter(NotificationsTypeFilter.All)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsContent(
    state: NotificationsUiState,
    onRefresh: () -> Unit,
    onSelectReadFilter: (NotificationsReadFilter) -> Unit,
    onSelectTypeFilter: (NotificationsTypeFilter) -> Unit,
    onMarkAllRead: () -> Unit,
    onMarkRead: (String) -> Unit,
    onDelete: (String) -> Unit,
    onOpenNotification: (AppNotification) -> Unit,
    onLoadMore: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    val visibleItems = state.visibleItems

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize().background(palette.background),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.fillMaxSize().widthIn(max = 780.dp)) {
                NotificationsHeader(
                    unreadCount = state.unreadCount,
                    isRefreshing = state.isRefreshing,
                    isMarkingAllRead = state.isMarkingAllRead,
                    onRefresh = onRefresh,
                    onMarkAllRead = onMarkAllRead,
                )
                HorizontalDivider(color = palette.borderSubtle)
                NotificationsFilterBar(
                    readFilter = state.readFilter,
                    typeFilter = state.typeFilter,
                    unreadCount = state.unreadCount,
                    onSelectReadFilter = onSelectReadFilter,
                    onSelectTypeFilter = onSelectTypeFilter,
                )

                when {
                    state.isLoading && state.items.isEmpty() -> NotificationsSkeletonList(
                        modifier = Modifier.weight(1f),
                    )
                    visibleItems.isEmpty() -> NotificationsEmptyView(
                        readFilter = state.readFilter,
                        typeFilter = state.typeFilter,
                        onResetFilters = onResetFilters,
                        modifier = Modifier.weight(1f),
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        items(visibleItems, key = { it.id }) { item ->
                            NotificationRowCard(
                                item = item,
                                isBusy = item.id in state.busyIds,
                                onOpen = { onOpenNotification(item) },
                                onMarkRead = { onMarkRead(item.id) },
                                onDelete = { onDelete(item.id) },
                            )
                        }
                        if (state.hasMore) {
                            item(key = "load_more") {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                ) {
                                    OutlinedButton(
                                        onClick = onLoadMore,
                                        enabled = !state.isLoadingMore,
                                        shape = RoundedCornerShape(14.dp),
                                    ) {
                                        if (state.isLoadingMore) {
                                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(
                                            AppStrings.loadOlderNotifications(state.items.size, state.totalElements),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(
    unreadCount: Int,
    isRefreshing: Boolean,
    isMarkingAllRead: Boolean,
    onRefresh: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LeoColors.primary.copy(alpha = 0.22f),
                                LeoColors.primaryLight.copy(alpha = 0.08f),
                            ),
                        ),
                    )
                    .border(1.dp, LeoColors.primary.copy(alpha = 0.26f), RoundedCornerShape(15.dp)),
            ) {
                Icon(Icons.Rounded.Notifications, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AppStrings.notifications,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusSummaryPill(unreadCount = unreadCount)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = AppStrings.notificationsSubtitle,
                    fontSize = 12.sp,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceElevated)
                    .border(1.dp, palette.border, RoundedCornerShape(12.dp))
                    .clickable(enabled = !isRefreshing, onClick = onRefresh)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = LeoColors.primary)
                } else {
                    Icon(Icons.Rounded.Refresh, contentDescription = AppStrings.refresh, tint = palette.textSecondary, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(AppStrings.refresh, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onMarkAllRead,
                enabled = unreadCount > 0 && !isMarkingAllRead,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LeoColors.primary,
                    contentColor = Color.White,
                    disabledContainerColor = palette.input,
                    disabledContentColor = palette.textMuted,
                ),
                modifier = Modifier.height(34.dp),
            ) {
                if (isMarkingAllRead) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(AppStrings.markAllRead, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatusSummaryPill(unreadCount: Int) {
    val hasUnread = unreadCount > 0
    val accent = if (hasUnread) LeoColors.primary else LeoColors.success
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        if (hasUnread) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(5.dp))
        }
        Text(
            text = if (hasUnread) AppStrings.unreadCountBadge(unreadCount) else AppStrings.allCaughtUp,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
    }
}

@Composable
private fun NotificationsFilterBar(
    readFilter: NotificationsReadFilter,
    typeFilter: NotificationsTypeFilter,
    unreadCount: Int,
    onSelectReadFilter: (NotificationsReadFilter) -> Unit,
    onSelectTypeFilter: (NotificationsTypeFilter) -> Unit,
) {
    val palette = LeoThemeTokens.palette
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.input)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(14.dp))
                .padding(4.dp),
        ) {
            ReadFilterTab(
                label = AppStrings.all,
                selected = readFilter == NotificationsReadFilter.All,
                badge = null,
                onClick = { onSelectReadFilter(NotificationsReadFilter.All) },
            )
            Spacer(Modifier.width(4.dp))
            ReadFilterTab(
                label = AppStrings.unread,
                selected = readFilter == NotificationsReadFilter.Unread,
                badge = unreadCount.takeIf { it > 0 },
                onClick = { onSelectReadFilter(NotificationsReadFilter.Unread) },
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            val chips = listOf(
                NotificationsTypeFilter.All to AppStrings.allTypes,
                NotificationsTypeFilter.Messages to AppStrings.messagesFilter,
                NotificationsTypeFilter.Reactions to AppStrings.reactionsFilter,
                NotificationsTypeFilter.Calls to AppStrings.callsFilter,
            )
            chips.forEach { (filter, label) ->
                val active = typeFilter == filter
                val bg by animateColorAsState(
                    targetValue = if (active) LeoColors.primary.copy(alpha = 0.14f) else palette.surfaceElevated,
                    animationSpec = tween(160),
                    label = "typeChipBg",
                )
                val border = if (active) LeoColors.primary.copy(alpha = 0.35f) else palette.borderSubtle
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) LeoColors.primary else palette.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(999.dp))
                        .clickable { onSelectTypeFilter(filter) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun ReadFilterTab(
    label: String,
    selected: Boolean,
    badge: Int?,
    onClick: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) palette.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) palette.textPrimary else palette.textSecondary,
        )
        if (badge != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(LeoColors.primary)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = if (badge > 99) "99+" else badge.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun NotificationRowCard(
    item: AppNotification,
    isBusy: Boolean,
    onOpen: () -> Unit,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val visual = visualFor(item.type)
    val cardBackground = when {
        item.isRead -> palette.surfaceElevated
        palette.isDark -> LeoColors.primary.copy(alpha = 0.09f)
        else -> palette.primarySoft.copy(alpha = 0.45f)
    }
    val cardBorder = if (item.isRead) palette.borderSubtle else LeoColors.primary.copy(alpha = 0.28f)
    val timeLabel = formatNotificationTime(item)

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBackground)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .clickable(enabled = !isBusy, onClick = onOpen)
            .padding(14.dp),
    ) {
        Box(Modifier.padding(top = 2.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(visual.accent.copy(alpha = 0.14f))
                    .border(1.dp, visual.accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
            ) {
                Icon(visual.icon, contentDescription = null, tint = visual.accent, modifier = Modifier.size(20.dp))
            }
            if (!item.isRead) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(LeoColors.primary)
                        .border(2.dp, palette.background, CircleShape),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontSize = 14.5.sp,
                    fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = visual.badgeLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = visual.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(visual.accent.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.body,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = palette.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeLabel,
                    fontSize = 11.5.sp,
                    color = palette.textMuted,
                    fontWeight = FontWeight.Medium,
                )
                if (!item.conversationId.isNullOrBlank()) {
                    Spacer(Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AppStrings.openChat,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LeoColors.primary,
                        )
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            tint = LeoColors.primary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (!item.isRead) {
                IconButton(
                    onClick = onMarkRead,
                    enabled = !isBusy,
                    modifier = Modifier.size(30.dp),
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = AppStrings.markAsRead,
                        tint = LeoColors.primary,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                enabled = !isBusy,
                modifier = Modifier.size(30.dp),
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = AppStrings.delete,
                    tint = palette.textMuted,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun NotificationsEmptyView(
    readFilter: NotificationsReadFilter,
    typeFilter: NotificationsTypeFilter,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    val hasFilter = readFilter == NotificationsReadFilter.Unread || typeFilter != NotificationsTypeFilter.All
    val title = when {
        readFilter == NotificationsReadFilter.Unread -> AppStrings.noUnreadTitle
        typeFilter != NotificationsTypeFilter.All -> AppStrings.noMatchingNotificationsTitle
        else -> AppStrings.noNotificationsTitle
    }
    val body = if (readFilter == NotificationsReadFilter.Unread) AppStrings.noUnreadBody else AppStrings.noNotificationsBody

    Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(palette.surfaceSubtle)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 36.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(palette.input),
            ) {
                Icon(Icons.Outlined.NotificationsOff, contentDescription = null, tint = palette.textMuted, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                body,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp),
            )
            AnimatedVisibility(visible = hasFilter) {
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(AppStrings.showAllNotifications, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun NotificationsSkeletonList(modifier: Modifier = Modifier) {
    val palette = LeoThemeTokens.palette
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        items(5) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surfaceElevated)
                    .border(1.dp, palette.borderSubtle, RoundedCornerShape(20.dp))
                    .padding(14.dp),
            ) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(palette.input))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth(0.45f).height(14.dp).clip(RoundedCornerShape(6.dp)).background(palette.input))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(0.85f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(palette.input))
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth(0.30f).height(10.dp).clip(RoundedCornerShape(6.dp)).background(palette.input))
                }
            }
        }
    }
}

private data class NotificationVisual(
    val badgeLabel: String,
    val icon: ImageVector,
    val accent: Color,
)

private fun visualFor(type: NotificationType): NotificationVisual = when (type) {
    NotificationType.IncomingCall -> NotificationVisual(
        badgeLabel = AppStrings.notificationCallBadge,
        icon = Icons.Rounded.Call,
        accent = LeoColors.success,
    )
    NotificationType.MessageReaction -> NotificationVisual(
        badgeLabel = AppStrings.notificationReactionBadge,
        icon = Icons.Rounded.Favorite,
        accent = LeoColors.destructive,
    )
    NotificationType.NewMessage, NotificationType.Unknown -> NotificationVisual(
        badgeLabel = AppStrings.notificationMessageBadge,
        icon = Icons.Rounded.ChatBubble,
        accent = LeoColors.primary,
    )
}

private fun formatNotificationTime(item: AppNotification): String {
    val instant = item.createdAt ?: return AppStrings.justNow
    val now = Clock.System.now()
    val elapsedMinutes = (now - instant).inWholeMinutes
    return when {
        elapsedMinutes < 1L -> AppStrings.justNow
        elapsedMinutes < 60L -> "${elapsedMinutes}m"
        else -> TimeFormat.relative(instant, now = now, yesterday = AppStrings.yesterday)
    }
}
