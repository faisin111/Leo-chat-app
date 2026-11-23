package com.leochat.features.auth.presentation.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.buttons.LeoTextLink
import com.leochat.core.widgets.common.feedback.LeoSnackBarController
import com.leochat.features.auth.presentation.states.AuthEffect
import com.leochat.resources.Res
import com.leochat.resources.connection_illustration
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource

private val WelcomeGlow = Color(0x28E64A19)

/** Hero illustration on the sign-in page. */
@Composable
fun WelcomeCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Image(
        painter = painterResource(Res.drawable.connection_illustration),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(354f / 218f)
            .shadow(14.dp, shape, ambientColor = WelcomeGlow, spotColor = WelcomeGlow)
            .clip(shape),
    )
}

/** Rounded tinted tile holding a feature icon (lock, key, …). */
@Composable
fun AuthIconBadge(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(56.dp).background(LeoColors.primarySoft, RoundedCornerShape(16.dp)),
    ) {
        Icon(icon, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(30.dp))
    }
}

/** "Prompt text  Action" row centered under auth forms. */
@Composable
fun AuthFooterPrompt(prompt: String, action: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$prompt ", color = LeoColors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        LeoTextLink(label = action, onClick = onAction)
    }
}

/**
 * Scrollable column that centers its content when it is shorter than the
 * viewport and caps width, like Flutter's `Center(SingleChildScrollView(ConstrainedBox))`.
 */
@Composable
fun CenteredScrollColumn(
    maxWidth: Dp,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    centerVertically: Boolean = true,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewport = this.maxHeight
        Box(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .heightIn(min = viewport)
                    .padding(contentPadding)
                    .widthIn(max = maxWidth)
                    .fillMaxWidth(),
                verticalArrangement = if (centerVertically) Arrangement.Center else Arrangement.Top,
                horizontalAlignment = horizontalAlignment,
                content = content,
            )
        }
    }
}

/** Routes controller effects to the app snack bar and navigation callbacks. */
@Composable
fun CollectAuthEffects(
    effects: Flow<AuthEffect>,
    snackBar: LeoSnackBarController,
    onNavigateHome: () -> Unit = {},
    onNavigateLogin: () -> Unit = {},
) {
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is AuthEffect.ShowError -> snackBar.showError(effect.message)
                is AuthEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                AuthEffect.NavigateHome -> onNavigateHome()
                AuthEffect.NavigateLogin -> onNavigateLogin()
            }
        }
    }
}
