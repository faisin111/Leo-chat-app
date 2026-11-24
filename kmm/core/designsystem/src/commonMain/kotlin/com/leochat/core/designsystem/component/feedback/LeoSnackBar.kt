package com.leochat.core.designsystem.component.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class LeoSnackBarType { Success, Error, Info }

data class LeoSnackBarVisuals(
    val title: String,
    override val message: String,
    val type: LeoSnackBarType,
) : SnackbarVisuals {
    override val actionLabel: String? = null
    override val withDismissAction: Boolean = true
    override val duration: SnackbarDuration = SnackbarDuration.Short
}

/** App-wide messenger. Mirrors the static `LeoSnackBar.showX` helpers. */
class LeoSnackBarController(
    val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    fun showSuccess(message: String, title: String = AppStrings.snackSuccess) = show(message, title, LeoSnackBarType.Success)
    fun showError(message: String, title: String = AppStrings.snackError) = show(message, title, LeoSnackBarType.Error)
    fun showInfo(message: String, title: String = AppStrings.snackNotice) = show(message, title, LeoSnackBarType.Info)

    fun show(message: String, title: String, type: LeoSnackBarType) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(LeoSnackBarVisuals(title, message, type))
        }
    }
}

val LocalLeoSnackBar = staticCompositionLocalOf<LeoSnackBarController> {
    error("LeoSnackBarController not provided")
}

@Composable
fun LeoSnackBarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier = modifier) { data -> LeoSnackBarCard(data) }
}

@Composable
private fun LeoSnackBarCard(data: SnackbarData) {
    val visuals = data.visuals as? LeoSnackBarVisuals
        ?: LeoSnackBarVisuals(AppStrings.snackNotice, data.visuals.message, LeoSnackBarType.Info)
    val (accent, icon) = when (visuals.type) {
        LeoSnackBarType.Success -> LeoColors.snackSuccess to Icons.Rounded.CheckCircle
        LeoSnackBarType.Error -> LeoColors.snackError to Icons.Rounded.ErrorOutline
        LeoSnackBarType.Info -> LeoColors.primary to Icons.Rounded.Info
    }
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = Color(0x38000000), spotColor = Color(0x38000000))
            .background(LeoColors.snackSurface, shape)
            .border(1.2.dp, accent.copy(alpha = 0.35f), shape)
            .padding(horizontal = 16.dp, vertical = 13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(38.dp).background(accent.copy(alpha = 0.16f), CircleShape),
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(visuals.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.2).sp)
            Spacer(Modifier.height(2.dp))
            Text(
                visuals.message,
                color = LeoColors.border,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (13 * 1.35).sp,
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { data.dismiss() }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = LeoColors.textMuted, modifier = Modifier.size(18.dp))
        }
    }
}
