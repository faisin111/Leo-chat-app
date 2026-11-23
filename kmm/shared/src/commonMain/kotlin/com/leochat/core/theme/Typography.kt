package com.leochat.core.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.leochat.resources.Res
import com.leochat.resources.poppins_black
import com.leochat.resources.poppins_bold
import com.leochat.resources.poppins_extrabold
import com.leochat.resources.poppins_regular
import com.leochat.resources.poppins_semibold
import org.jetbrains.compose.resources.Font

/** Bundled Poppins family. 500 falls back to Regular, as in the Flutter build. */
@Composable
fun poppinsFontFamily(): FontFamily = FontFamily(
    Font(Res.font.poppins_regular, FontWeight.Normal),
    Font(Res.font.poppins_regular, FontWeight.Medium),
    Font(Res.font.poppins_semibold, FontWeight.SemiBold),
    Font(Res.font.poppins_bold, FontWeight.Bold),
    Font(Res.font.poppins_extrabold, FontWeight.ExtraBold),
    Font(Res.font.poppins_black, FontWeight.Black),
)

/** Material 3 type scale with Poppins applied to every style. */
@Composable
fun leoTypography(): Typography {
    val family = poppinsFontFamily()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )
}

/** Shared named styles. Mirrors `lib/core/theme/typography.dart`. */
object LeoTypography {
    /** Extra-bold display heading used for primary page titles. */
    val mainHeading = TextStyle(
        fontSize = 38.sp,
        lineHeight = (38 * 1.08).sp,
        letterSpacing = (-1.4).sp,
        fontWeight = FontWeight.Black,
        color = LeoColors.textPrimary,
    )
}

/** Converts a Flutter `height` multiplier into an absolute Compose line height. */
fun lineHeight(fontSize: Int, multiplier: Double): TextUnit = (fontSize * multiplier).sp
