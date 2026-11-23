package com.leochat.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.leochat.app.navigation.LeoNavHost
import com.leochat.app.navigation.goTo
import com.leochat.core.constants.AppRoutes
import com.leochat.core.constants.AppStrings
import com.leochat.core.responsive.ProvideLeoLayout
import com.leochat.core.session.SessionEvents
import com.leochat.core.theme.LeoTheme
import com.leochat.core.theme.ThemeModeStore
import com.leochat.core.widgets.common.feedback.LeoSnackBarController
import com.leochat.core.widgets.common.feedback.LeoSnackBarHost
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import org.koin.compose.koinInject

/** Root composable shared by Android and iOS. Mirrors `LeoChatApp` in Flutter. */
@Composable
fun LeoChatApp() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
    }

    val themeStore = koinInject<ThemeModeStore>()
    val sessionEvents = koinInject<SessionEvents>()
    val themeMode by themeStore.mode.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackBar = remember { LeoSnackBarController(SnackbarHostState(), scope) }

    LaunchedEffect(sessionEvents) {
        sessionEvents.sessionExpired.collect {
            snackBar.showInfo(AppStrings.sessionExpired)
            navController.goTo(AppRoutes.Login)
        }
    }

    LeoTheme(mode = themeMode) {
        CompositionLocalProvider(LocalLeoSnackBar provides snackBar) {
            ProvideLeoLayout {
                Box(Modifier.fillMaxSize()) {
                    LeoNavHost(navController)
                    LeoSnackBarHost(
                        hostState = snackBar.hostState,
                        modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                    )
                }
            }
        }
    }
}
