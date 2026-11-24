package com.leochat.features.admin.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AdminStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.theme.lineHeight
import com.leochat.features.admin.domain.entities.AdminStats

/** Dark gradient "Overview & Moderation" hero card. */
@Composable
fun AdminWelcomeBanner(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, ambientColor = AdminTokens.bannerShadow, spotColor = AdminTokens.bannerShadow)
            .background(Brush.horizontalGradient(listOf(AdminTokens.bannerStart, AdminTokens.bannerEnd)), shape)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(LeoColors.primary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    AdminStrings.administratorBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.8.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = LeoColors.success, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                AdminStrings.systemsOperational,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AdminTokens.bannerMuted,
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(AdminStrings.overviewTitle, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(Modifier.height(6.dp))
        Text(
            AdminStrings.overviewSubtitle,
            fontSize = 13.sp,
            color = AdminTokens.bannerMuted,
            lineHeight = lineHeight(13, 1.4),
        )
    }
}

/**
 * KPI cards. Mobile: users + conversations side by side, reports full width (Flutter layout).
 * Tablet/desktop ([wide]): all three on one row.
 */
@Composable
fun AdminStatsGrid(
    stats: AdminStats,
    wide: Boolean,
    onUsers: () -> Unit,
    onConversations: () -> Unit,
    onReports: () -> Unit,
) {
    val users: @Composable (Modifier) -> Unit = {
        AdminStatCard(AdminStrings.totalUsers, stats.totalUsers.toString(), Icons.Rounded.PeopleAlt, LeoColors.info, onUsers, it)
    }
    val conversations: @Composable (Modifier) -> Unit = {
        AdminStatCard(AdminStrings.conversations, stats.totalConversations.toString(), Icons.Rounded.Forum, LeoColors.primary, onConversations, it)
    }
    val reports: @Composable (Modifier) -> Unit = {
        AdminStatCard(AdminStrings.reportsAndFlagged, stats.totalReports.toString(), Icons.Rounded.Flag, LeoColors.destructive, onReports, it)
    }
    if (wide) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            users(Modifier.weight(1f))
            conversations(Modifier.weight(1f))
            reports(Modifier.weight(1f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                users(Modifier.weight(1f))
                conversations(Modifier.weight(1f))
            }
            reports(Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .adminPressable(onClick)
            .background(palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp).background(color.copy(alpha = AdminTokens.tintAlpha), RoundedCornerShape(10.dp)),
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = AdminTokens.chevron,
                modifier = Modifier.size(13.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Black, color = palette.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textSecondary, maxLines = 1)
    }
}

/** "MANAGEMENT MODULES" header plus the users/reports navigation card. */
@Composable
fun AdminModulesSection(onUsers: () -> Unit, onReports: () -> Unit) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(20.dp)
    Column {
        Text(
            AdminStrings.managementModules,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
            color = palette.textSecondary,
        )
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().background(palette.surface, shape).border(1.dp, palette.border, shape)) {
            AdminNavTile(
                icon = Icons.Rounded.ManageAccounts,
                title = AdminStrings.userManagementTile,
                subtitle = AdminStrings.userManagementSubtitle,
                onClick = onUsers,
            )
            HorizontalDivider(thickness = 1.dp, color = palette.input, modifier = Modifier.padding(start = 64.dp))
            AdminNavTile(
                icon = Icons.Rounded.Gavel,
                title = AdminStrings.reportModerationTile,
                subtitle = AdminStrings.reportModerationSubtitle,
                onClick = onReports,
            )
        }
    }
}

@Composable
fun AdminNavTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().adminPressable(onClick).padding(16.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(42.dp).background(palette.primarySoft, RoundedCornerShape(12.dp)),
        ) {
            Icon(icon, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = palette.textSecondary)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AdminTokens.chevron)
    }
}
