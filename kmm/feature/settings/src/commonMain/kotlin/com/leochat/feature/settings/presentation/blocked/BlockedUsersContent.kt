package com.leochat.feature.settings.presentation.blocked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.feedback.LeoShimmer
import com.leochat.core.designsystem.component.feedback.LeoSkeleton
import com.leochat.core.designsystem.component.feedback.LeoSkeletonCircle
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.settings.SettingsAppBar
import com.leochat.core.designsystem.component.settings.SettingsCard
import com.leochat.core.designsystem.component.settings.SettingsDivider
import com.leochat.core.designsystem.component.settings.SettingsIconBadge
import com.leochat.core.designsystem.component.settings.SettingsTextScope
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.settings.domain.model.BlockedUserItem
import com.leochat.feature.settings.presentation.settings.BlockedUsersUiState
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Stateless blocked-users list layout mirroring Web `BlockedUsersPage`. */
@Composable
fun BlockedUsersContent(
    state: BlockedUsersUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(
                    title = SettingsStrings.blockedPeople,
                    onBack = onBack,
                    background = palette.background,
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(
                        SettingsStrings.blockedPeopleSubtitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(bottom = 14.dp),
                    )
                    when {
                        state.isLoading -> BlockedUsersSkeleton()
                        state.error != null -> BlockedUsersErrorCard(state.error, onRetry)
                        state.users.isEmpty() -> BlockedUsersEmptyCard()
                        else -> SettingsCard {
                            state.users.forEachIndexed { index, user ->
                                BlockedUserRow(user)
                                if (index != state.users.lastIndex) SettingsDivider()
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = palette.textSecondary,
                            modifier = Modifier.padding(top = 2.dp).size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            SettingsStrings.blockedReadOnlyNote,
                            fontSize = 12.sp,
                            lineHeight = 1.4.em,
                            color = palette.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BlockedUserRow(user: BlockedUserItem) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        LeoAvatar(radius = 22.dp, name = user.effectiveName, imageUrl = user.avatarUrl)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                user.effectiveName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = buildList {
                user.username?.takeIf { it.isNotBlank() }?.let { add("@$it") }
                user.blockedAt?.let { add("${SettingsStrings.blockedOnPrefix}${formatDate(it)}") }
            }.joinToString(SettingsStrings.separator)
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun BlockedUsersEmptyCard() {
    val palette = LeoThemeTokens.palette
    SettingsCard(padding = 28.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SettingsIconBadge(
                icon = Icons.Outlined.Shield,
                tint = LeoColors.primary,
                background = LeoColors.primary.copy(alpha = 0.12f),
                size = 48.dp,
                radius = 14.dp,
                iconSize = 24.dp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                SettingsStrings.blockedPeopleEmpty,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                SettingsStrings.blockedPeopleEmptyBody,
                fontSize = 13.sp,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BlockedUsersErrorCard(error: String, onRetry: () -> Unit) {
    val palette = LeoThemeTokens.palette
    SettingsCard(padding = 20.dp) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                SettingsStrings.blockedPeopleError,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary,
            )
            Text(error, fontSize = 13.sp, color = palette.textSecondary)
            LeoPrimaryButton(label = SettingsStrings.retry, onClick = onRetry, modifier = Modifier.fillMaxWidth().height(46.dp))
        }
    }
}

@Composable
private fun BlockedUsersSkeleton() {
    LeoShimmer(modifier = Modifier.fillMaxWidth()) {
        SettingsCard {
            repeat(3) { idx ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    LeoSkeletonCircle(size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LeoSkeleton(modifier = Modifier.width(140.dp).height(14.dp), cornerRadius = 6.dp)
                        LeoSkeleton(modifier = Modifier.width(100.dp).height(12.dp), cornerRadius = 6.dp)
                    }
                }
                if (idx != 2) SettingsDivider()
            }
        }
    }
}

private fun formatDate(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${local.year}-${local.monthNumber.toString().padStart(2, '0')}-${local.dayOfMonth.toString().padStart(2, '0')}"
}
