package com.leochat.features.chat.presentation.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.resources.Res
import com.leochat.resources.avatar_portrait
import kotlin.math.PI
import kotlin.math.sin

private val TypingShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp)

/** Incoming "X is typing..." bubble with three bouncing dots. Mirrors `LeoTypingIndicator`. */
@Composable
fun TypingIndicator(text: String?, avatarUrl: String?, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val progress by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "typingProgress")

    Row(modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.Bottom) {
        LeoAvatar(radius = 14.dp, imageUrl = avatarUrl, drawable = Res.drawable.avatar_portrait)
        Spacer(Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .shadow(4.dp, TypingShape, ambientColor = Color(0x0D000000), spotColor = Color(0x0D000000))
                .background(Color.White, TypingShape)
                .border(1.dp, Color(0xFFEFE8E2), TypingShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(text ?: AppStrings.typingSuffix, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LeoColors.textSecondary)
            Spacer(Modifier.width(8.dp))
            repeat(3) { index ->
                val phase = (progress + index * 0.18f) % 1f
                val bounce = sin(phase * 2 * PI).toFloat()
                val dy = if (bounce < 0) bounce * 4.5f else 0f
                val alpha = 0.45f + (-bounce).coerceIn(0f, 1f) * 0.55f
                Box(
                    Modifier
                        .padding(horizontal = 2.2.dp)
                        .graphicsLayer { translationY = dy * density }
                        .alpha(alpha)
                        .size(6.5.dp)
                        .background(LeoColors.primary, CircleShape),
                )
            }
        }
    }
}
