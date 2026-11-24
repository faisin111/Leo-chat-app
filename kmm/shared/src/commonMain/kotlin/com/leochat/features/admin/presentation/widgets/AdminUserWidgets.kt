package com.leochat.features.admin.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AdminStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.features.admin.domain.entities.AdminUser

/** Status chip color: green ACTIVE, amber DISABLED, red BANNED. */
fun AdminUser.statusColor(): Color = when {
    isBanned -> LeoColors.destructive
    isDisabled -> AdminTokens.amber
    else -> LeoColors.success
}

/** Bordered search box with a leading search icon. */
@Composable
fun AdminSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(start = 12.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.textSecondary)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(AdminStrings.searchUsersHint, fontSize = 14.sp, color = palette.textMuted, maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = palette.textPrimary),
                cursorBrush = SolidColor(LeoColors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun AdminUserTile(user: AdminUser, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .adminPressable(onClick)
            .background(palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(14.dp),
    ) {
        LeoAvatar(radius = 22.dp, name = user.effectiveName, imageUrl = user.profilePictureUrl)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    user.effectiveName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (user.isAdmin) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .background(LeoColors.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.5.dp),
                    ) {
                        Text(AdminStrings.adminBadge, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.size(2.dp))
            Text(
                AdminStrings.handleWithEmail(user.username, user.email),
                fontSize = 12.sp,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AdminStatusBadge(user.status, user.statusColor())
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Rounded.MoreVert, contentDescription = AdminStrings.userActions, tint = AdminTokens.chevron, modifier = Modifier.size(20.dp))
    }
}

/** Callbacks for the per-user moderation sheet. Each one closes the sheet first. */
data class AdminUserActions(
    val onMarkActive: (AdminUser) -> Unit,
    val onMarkDisabled: (AdminUser) -> Unit,
    val onMarkBanned: (AdminUser) -> Unit,
    val onForceLogout: (AdminUser) -> Unit,
)

/** Bottom sheet listing the status changes that apply to [user] plus force logout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserActionsSheet(user: AdminUser, actions: AdminUserActions, onDismiss: () -> Unit) {
    val palette = LeoThemeTokens.palette
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 40.dp, height = 4.dp).background(AdminTokens.sheetHandle, RoundedCornerShape(2.dp)))
            }
            Spacer(Modifier.size(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LeoAvatar(radius = 20.dp, name = user.effectiveName, imageUrl = user.profilePictureUrl)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(user.effectiveName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
                    Text(AdminStrings.handleWithStatus(user.username, user.status), fontSize = 13.sp, color = palette.textSecondary)
                }
            }
            Spacer(Modifier.size(20.dp))
            val act: ((AdminUser) -> Unit) -> () -> Unit = { action ->
                {
                    onDismiss()
                    action(user)
                }
            }
            if (!user.isActive) {
                SheetAction(Icons.Outlined.CheckCircleOutline, LeoColors.success, AdminStrings.markActive, act(actions.onMarkActive))
            }
            if (!user.isDisabled) {
                SheetAction(Icons.Outlined.PauseCircleOutline, AdminTokens.amber, AdminStrings.markDisabled, act(actions.onMarkDisabled))
            }
            if (!user.isBanned) {
                SheetAction(Icons.Rounded.Block, LeoColors.destructive, AdminStrings.markBanned, act(actions.onMarkBanned))
            }
            SheetAction(Icons.AutoMirrored.Rounded.Logout, AdminTokens.logoutIcon, AdminStrings.forceLogout, act(actions.onForceLogout))
        }
    }
}

/** Material `ListTile` equivalent: 56dp row, 24dp leading icon, 16sp title. */
@Composable
private fun SheetAction(icon: ImageVector, tint: Color, title: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = LeoThemeTokens.palette.textPrimary)
    }
}
