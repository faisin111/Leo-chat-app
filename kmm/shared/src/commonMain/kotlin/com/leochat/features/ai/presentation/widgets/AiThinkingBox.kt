package com.leochat.features.ai.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AiStrings

/** Collapsible reasoning trace above an assistant reply. Mirrors `AiThinkingBox`. */
@Composable
fun AiThinkingBox(thinkingContent: String, durationSecs: Double?, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .background(AiTokens.slate50, shape)
            .border(1.dp, AiTokens.slate200, shape),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .aiPressable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Icon(Icons.Rounded.Psychology, contentDescription = null, tint = AiTokens.slate500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                AiStrings.thinkingProcess(formatDuration(durationSecs)),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AiTokens.slate600,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = AiTokens.slate500,
                modifier = Modifier.size(18.dp),
            )
        }
        if (expanded) {
            Text(
                thinkingContent,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = (12 * 1.5).sp,
                color = AiTokens.slate700,
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            )
        }
    }
}

/** `toStringAsFixed(1)` + `s`, or empty when unknown. */
internal fun formatDuration(seconds: Double?): String {
    if (seconds == null) return ""
    // Half-up rounding, as Dart's `toStringAsFixed`.
    val tenths = kotlin.math.floor(kotlin.math.abs(seconds) * 10 + 0.5).toLong()
    return "${tenths / 10}.${tenths % 10}s"
}
