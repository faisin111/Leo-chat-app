package com.leochat.core.designsystem.component.branding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.resources.Res
import com.leochat.resources.leo_chat_icon
import org.jetbrains.compose.resources.painterResource

private val LogoGlow = Color(0x26F35A26)

/** App mark plus wordmark. Mirrors `LeoLogo`; uses the bundled icon only. */
@Composable
fun LeoLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onDark: Boolean = false,
) {
    val palette = LeoThemeTokens.palette
    val size = if (compact) 36.dp else 42.dp
    val shape = RoundedCornerShape(if (compact) 11.dp else 13.dp)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(Res.drawable.leo_chat_icon),
            contentDescription = AppStrings.appName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .shadow(elevation = 9.dp, shape = shape, ambientColor = LogoGlow, spotColor = LogoGlow)
                .clip(shape),
        )
        if (!compact) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = AppStrings.appName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.6).sp,
                color = if (onDark || palette.isDark) Color.White else palette.textPrimary,
            )
        }
    }
}
