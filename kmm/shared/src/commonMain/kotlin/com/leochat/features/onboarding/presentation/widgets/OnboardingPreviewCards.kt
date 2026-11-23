package com.leochat.features.onboarding.presentation.widgets

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
import com.leochat.core.theme.LeoColors

private val CardShape = RoundedCornerShape(24.dp)
private val CardShadow = Color(0x120F172A)
private val PdfBackground = Color(0xFFFEE2E2)
private val PdfBorder = Color(0xFFFCA5A5)
private val PdfText = Color(0xFFB91C1C)
private val DeviceTile = Color(0xFF1E293B)

@Composable
private fun PreviewCard(
    padding: Dp,
    background: Color = Color.White,
    shadowColor: Color = CardShadow,
    bordered: Boolean = true,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = Modifier
        .widthIn(max = 400.dp)
        .fillMaxWidth()
        .height(230.dp)
        .shadow(16.dp, CardShape, ambientColor = shadowColor, spotColor = shadowColor)
        .background(background, CardShape)
    Column(
        modifier = (if (bordered) base.border(1.dp, LeoColors.border, CardShape) else base).padding(padding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

/** Slide 1: a mini conversation with reactions. */
@Composable
fun ChatPreviewCard() {
    PreviewCard(padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).background(LeoColors.primarySoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Sarah Jenkins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Circle, contentDescription = null, tint = LeoColors.success, modifier = Modifier.size(7.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Active now", fontSize = 10.sp, color = LeoColors.textMuted)
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "STOMP",
                fontSize = 9.sp,
                color = LeoColors.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.background(LeoColors.primarySoft, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Coffee before the demo? ☕",
            fontSize = 12.sp,
            color = LeoColors.textPrimary,
            modifier = Modifier.background(LeoColors.incomingBubble, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 9.dp),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.align(Alignment.End).background(LeoColors.primary, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Yes! 10:30 works ✨", fontSize = 12.sp, color = Color.White)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.DoneAll, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.align(Alignment.End)) {
            ReactionChip("❤️", "3")
            Spacer(Modifier.width(5.dp))
            ReactionChip("🔥", "2")
        }
    }
}

/** Slide 2: devices kept in sync. */
@Composable
fun SyncPreviewCard() {
    PreviewCard(
        padding = 20.dp,
        background = LeoColors.rail,
        shadowColor = Color(0x22020617),
        bordered = false,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            DeviceIconBox(Icons.Filled.PhoneIphone, "Mobile")
            Box(Modifier.background(LeoColors.primary.copy(alpha = 0.2f), CircleShape).padding(8.dp)) {
                Icon(Icons.Rounded.Sync, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(24.dp))
            }
            DeviceIconBox(Icons.Filled.LaptopMac, "Desktop")
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
                Text("Synced & Offline Ready", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Local cache stores messages securely", color = LeoColors.textMuted, fontSize = 10.sp)
            }
        }
    }
}

/** Slide 3: files, voice notes, emoji reactions. */
@Composable
fun ExpressionPreviewCard() {
    PreviewCard(padding = 18.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PdfBackground, RoundedCornerShape(12.dp))
                .border(1.dp, PdfBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = PdfText, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "LeoChat_Architecture.pdf",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PdfText,
                modifier = Modifier.weight(1f),
            )
            Text("1.4 MB", fontSize = 10.sp, color = PdfText)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(LeoColors.primarySoft, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.PlayCircleFilled, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("lıllıılııllıııl 0:24", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LeoColors.primary, modifier = Modifier.weight(1f))
            Text("Voice note", fontSize = 10.sp, color = LeoColors.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("👍", "❤️", "🔥", "🎉", "🚀").forEach { EmojiBubble(it) }
        }
    }
}

@Composable
private fun DeviceIconBox(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .background(DeviceTile, RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = LeoColors.textMuted, fontSize = 11.sp)
    }
}

@Composable
private fun ReactionChip(emoji: String, count: String) {
    Row(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, LeoColors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 11.sp)
        Spacer(Modifier.width(3.dp))
        Text(count, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary)
    }
}

@Composable
private fun EmojiBubble(emoji: String) {
    Box(
        modifier = Modifier
            .background(LeoColors.background, CircleShape)
            .border(1.dp, LeoColors.border, CircleShape)
            .padding(8.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
    }
}
