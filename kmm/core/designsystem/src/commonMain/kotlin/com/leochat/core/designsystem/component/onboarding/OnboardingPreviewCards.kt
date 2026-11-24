package com.leochat.core.designsystem.component.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.OnboardingStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.OnboardingTokens

private val CardShape = RoundedCornerShape(24.dp)

@Composable
private fun PreviewCard(
    padding: Dp,
    background: Color = Color.Unspecified,
    shadowColor: Color = OnboardingTokens.cardShadow,
    bordered: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val resolvedBg = if (background != Color.Unspecified) background else palette.surface
    val base = Modifier
        .widthIn(max = 400.dp)
        .fillMaxWidth()
        .height(230.dp)
        .shadow(16.dp, CardShape, ambientColor = shadowColor, spotColor = shadowColor)
        .background(resolvedBg, CardShape)
    Column(
        modifier = (if (bordered) base.border(1.dp, palette.border, CardShape) else base).padding(padding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

/** Slide 1: a mini conversation with reactions. */
@Composable
fun ChatPreviewCard() {
    val palette = LeoThemeTokens.palette
    PreviewCard(padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).background(palette.primarySoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(OnboardingStrings.chatContactName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Circle, contentDescription = null, tint = LeoColors.success, modifier = Modifier.size(7.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(OnboardingStrings.chatContactStatus, fontSize = 10.sp, color = palette.textMuted)
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                OnboardingStrings.chatTransportBadge,
                fontSize = 9.sp,
                color = LeoColors.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.background(palette.primarySoft, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            OnboardingStrings.chatIncomingMessage,
            fontSize = 12.sp,
            color = palette.textPrimary,
            modifier = Modifier.background(palette.incomingBubble, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 9.dp),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.align(Alignment.End).background(LeoColors.primary, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(OnboardingStrings.chatOutgoingMessage, fontSize = 12.sp, color = Color.White)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.DoneAll, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.align(Alignment.End)) {
            ReactionChip(OnboardingStrings.reactionHeart, OnboardingStrings.reactionHeartCount)
            Spacer(Modifier.width(5.dp))
            ReactionChip(OnboardingStrings.reactionFire, OnboardingStrings.reactionFireCount)
        }
    }
}

/** Slide 2: devices kept in sync. */
@Composable
fun SyncPreviewCard() {
    PreviewCard(
        padding = 20.dp,
        background = Color(0xFF020617),
        shadowColor = OnboardingTokens.syncCardShadow,
        bordered = false,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            DeviceIconBox(Icons.Filled.PhoneIphone, OnboardingStrings.syncMobile)
            Box(Modifier.background(LeoColors.primary.copy(alpha = 0.2f), CircleShape).padding(8.dp)) {
                Icon(Icons.Rounded.Sync, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(24.dp))
            }
            DeviceIconBox(Icons.Filled.LaptopMac, OnboardingStrings.syncDesktop)
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = LeoColors.success, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(OnboardingStrings.syncTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(OnboardingStrings.syncBody, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        }
    }
}

/** Slide 3: files, voice notes, emoji reactions. */
@Composable
fun ExpressionPreviewCard() {
    val palette = LeoThemeTokens.palette
    PreviewCard(padding = 18.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OnboardingTokens.pdfBackground, RoundedCornerShape(12.dp))
                .border(1.dp, OnboardingTokens.pdfBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = OnboardingTokens.pdfText, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                OnboardingStrings.fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OnboardingTokens.pdfText,
                modifier = Modifier.weight(1f),
            )
            Text(OnboardingStrings.fileSize, fontSize = 10.sp, color = OnboardingTokens.pdfText)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(palette.primarySoft, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PlayCircleFilled, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(OnboardingStrings.voiceWaveform, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LeoColors.primary, modifier = Modifier.weight(1f))
            Text(OnboardingStrings.voiceLabel, fontSize = 10.sp, color = LeoColors.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OnboardingStrings.emojiReactions.forEach { EmojiBubble(it) }
        }
    }
}

@Composable
private fun DeviceIconBox(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(OnboardingTokens.deviceTile, RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = LeoThemeTokens.palette.textMuted, fontSize = 11.sp)
    }
}

@Composable
private fun ReactionChip(emoji: String, count: String) {
    val palette = LeoThemeTokens.palette
    Row(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = OnboardingTokens.reactionShadow, spotColor = OnboardingTokens.reactionShadow)
            .background(palette.surface, RoundedCornerShape(12.dp))
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 11.sp)
        Spacer(Modifier.width(3.dp))
        Text(count, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
    }
}

@Composable
private fun EmojiBubble(emoji: String) {
    val palette = LeoThemeTokens.palette
    Box(
        modifier = Modifier
            .background(palette.background, CircleShape)
            .border(1.dp, palette.border, CircleShape)
            .padding(8.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
    }
}
