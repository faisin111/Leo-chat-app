package com.leochat.core.designsystem.theme

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
fun poppinsFontFamily(): FontFamily {
    val regular = Font(Res.font.poppins_regular, FontWeight.Normal)
    val medium = Font(Res.font.poppins_regular, FontWeight.Medium)
    val semiBold = Font(Res.font.poppins_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.poppins_bold, FontWeight.Bold)
    val extraBold = Font(Res.font.poppins_extrabold, FontWeight.ExtraBold)
    val black = Font(Res.font.poppins_black, FontWeight.Black)
    return androidx.compose.runtime.remember {
        FontFamily(regular, medium, semiBold, bold, extraBold, black)
    }
}

/** Material 3 type scale with Poppins applied to every style, aligned with LEO_CHAT_FRONTEND_GUIDE. */
@Composable
fun leoTypography(): Typography {
    val family = poppinsFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontSize = 34.sp,
            lineHeight = (34 * 1.15).sp,
            letterSpacing = (-1.0).sp,
            fontWeight = FontWeight.Bold,
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontSize = 28.sp,
            lineHeight = (28 * 1.15).sp,
            letterSpacing = (-0.6).sp,
            fontWeight = FontWeight.Bold,
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.4).sp,
            fontWeight = FontWeight.SemiBold,
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.4).sp,
            fontWeight = FontWeight.Bold,
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.2).sp,
            fontWeight = FontWeight.SemiBold,
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.15).sp,
            fontWeight = FontWeight.SemiBold,
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.4.sp,
            fontWeight = FontWeight.SemiBold,
        ),
    )
}

/** Shared named styles. Mirrors `lib/core/theme/typography.dart` and frontend guide. */
object LeoTypography {
    /** Bold display heading used for primary page titles. */
    val mainHeading: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 32.sp,
            lineHeight = (32 * 1.15).sp,
            letterSpacing = (-0.8).sp,
            fontWeight = FontWeight.Bold,
            color = LeoThemeTokens.palette.textPrimary,
        )

    /** Display header (24px, SemiBold, 32px line height). */
    val displayHeader: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.4).sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoThemeTokens.palette.textPrimary,
        )

    /** Section header (18px, SemiBold, 24px line height). */
    val sectionHeader: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.15).sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoThemeTokens.palette.textPrimary,
        )

    /** Conversation title (14px, SemiBold, 20px line height). */
    val conversationTitle: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoThemeTokens.palette.textPrimary,
        )

    /** Message body (14px, Regular, 20px line height). */
    val messageBody: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = LeoThemeTokens.palette.textPrimary,
        )

    /** Subtext / snippet (12px, Regular, 16px line height). */
    val subtext: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            color = LeoThemeTokens.palette.textSecondary,
        )

    /** Timestamp / badge (10px, SemiBold, 14px line height). */
    val badge: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.2.sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoThemeTokens.palette.textSecondary,
        )

    /** Label / category (11px, SemiBold, 14px line height, uppercase tracking). */
    val categoryLabel: TextStyle
        @Composable
        get() = TextStyle(
            fontFamily = poppinsFontFamily(),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.4.sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoThemeTokens.palette.textSecondary,
        )
}

/** Converts a Flutter `height` multiplier into an absolute Compose line height. */
fun lineHeight(fontSize: Int, multiplier: Double): TextUnit = (fontSize * multiplier).sp
