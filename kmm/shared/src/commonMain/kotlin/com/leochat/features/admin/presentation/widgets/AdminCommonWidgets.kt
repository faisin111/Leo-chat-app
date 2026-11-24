package com.leochat.features.admin.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AdminStrings
import com.leochat.core.responsive.LeoLayout
import com.leochat.core.responsive.LocalLeoLayout
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.buttons.LeoOutlineButton
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.admin.presentation.states.AdminEffect
import kotlinx.coroutines.flow.Flow

/**
 * Admin-console accents taken verbatim from `lib/features/admin` (slate neutrals,
 * the dark console banner, and Material amber for the DISABLED status).
 */
object AdminTokens {
    val chevron = Color(0xFF94A3B8)
    val sheetHandle = Color(0xFFCBD5E1)
    val bannerStart = Color(0xFF1E1614)
    val bannerEnd = Color(0xFF2E2422)
    val bannerShadow = Color(0x22000000)
    val bannerMuted = Color.White.copy(alpha = 0.70f)
    val resolutionSurface = Color(0xFFF1F5F9)
    val resolutionText = Color(0xFF475569)
    val logoutIcon = Color(0xFF64748B)
    val amber = Color(0xFFFFC107)
    val tintAlpha = 0.12f

    /** Content column width cap so tablet/desktop keep a readable, centered list. */
    fun maxContentWidth(layout: LeoLayout): Dp = when (layout) {
        LeoLayout.Mobile -> Dp.Unspecified
        LeoLayout.Tablet -> 720.dp
        LeoLayout.Desktop -> 960.dp
    }
}

/** Tap with the shared LeoChat press-scale effect and no ripple (Flutter `LeoPressable`). */
@Composable
fun Modifier.adminPressable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this
        .leoPressable(interaction)
        .clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

/** White app bar: back arrow, 20sp extra-bold title, optional trailing actions. */
@Composable
fun AdminTopBar(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(palette.surface).height(56.dp).padding(horizontal = 4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = AdminStrings.back, tint = palette.textPrimary)
        }
        Spacer(Modifier.size(12.dp))
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        actions()
    }
}

/** Centers its content and caps the width on tablet/desktop. */
@Composable
fun AdminContentFrame(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val maxWidth = AdminTokens.maxContentWidth(LocalLeoLayout.current)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.fillMaxSize().widthIn(max = maxWidth), content = content)
    }
}

/** Horizontal status filter pills (`ALL`, `ACTIVE`, ...). */
@Composable
fun AdminFilterPills(
    filters: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LeoThemeTokens.palette
    LazyRow(
        modifier = modifier.fillMaxWidth().height(40.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(filters, key = { it }) { filter ->
            val isSelected = filter == selected
            val shape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .adminPressable { onSelect(filter) }
                    .background(if (isSelected) LeoColors.primary else palette.surface, shape)
                    .border(1.dp, if (isSelected) LeoColors.primary else palette.border, shape)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    filter,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else palette.textSecondary,
                )
            }
        }
    }
}

/** Tinted uppercase status chip (12% fill, solid label). */
@Composable
fun AdminStatusBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = AdminTokens.tintAlpha), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun AdminLoading(modifier: Modifier = Modifier, padding: Dp = 0.dp) {
    Box(modifier.fillMaxWidth().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = LeoColors.primary)
    }
}

/** Centered secondary-text message used for empty lists. */
@Composable
fun AdminCenteredMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 14.sp, color = LeoThemeTokens.palette.textSecondary, textAlign = TextAlign.Center)
    }
}

/** Load failure with the Flutter message copy plus a retry action. */
@Composable
fun AdminErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, fontSize = 14.sp, color = LeoThemeTokens.palette.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        LeoOutlineButton(label = AdminStrings.retry, onClick = onRetry, modifier = Modifier.widthIn(max = 220.dp))
    }
}

/** Permission state shown when the user is not an admin or the API answered 403. */
@Composable
fun AdminForbiddenState(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LeoThemeTokens.palette
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp).background(palette.primarySoft, RoundedCornerShape(22.dp)),
        ) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            AdminStrings.forbiddenTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            AdminStrings.forbiddenBody,
            fontSize = 14.sp,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = (14 * 1.4).sp,
        )
        Spacer(Modifier.height(24.dp))
        LeoPrimaryButton(label = AdminStrings.goBack, onClick = onBack, modifier = Modifier.widthIn(max = 260.dp))
    }
}

/** Routes controller effects to the app-wide snack bar. */
@Composable
fun CollectAdminEffects(effects: Flow<AdminEffect>) {
    val snackBar = LocalLeoSnackBar.current
    androidx.compose.runtime.LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is AdminEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                is AdminEffect.ShowError -> snackBar.showError(effect.message)
            }
        }
    }
}
