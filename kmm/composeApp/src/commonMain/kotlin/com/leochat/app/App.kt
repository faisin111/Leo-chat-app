package com.leochat.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.leochat.app.navigation.AppNavHost
import com.leochat.core.navigation.goTo
import com.leochat.core.navigation.AppRoutes
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.responsive.LeoLayout
import com.leochat.core.designsystem.responsive.LocalLeoLayout
import com.leochat.core.designsystem.responsive.ProvideLeoLayout
import com.leochat.core.network.auth.SessionEvents
import com.leochat.core.designsystem.theme.LeoTheme
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.ThemeModeStore
import com.leochat.core.designsystem.component.feedback.LeoSnackBarController
import com.leochat.core.designsystem.component.feedback.LeoSnackBarHost
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.calls.presentation.CallController
import com.leochat.feature.calls.presentation.incomingcall.IncomingCallDialog
import org.koin.compose.koinInject

/** Root composable shared by Android and iOS. Mirrors `LeoChatApp` in Flutter. */
@Composable
fun LeoChatApp() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
    }

    val themeStore = koinInject<ThemeModeStore>()
    val sessionEvents = koinInject<SessionEvents>()
    val callController = koinInject<CallController>()
    val themeMode by themeStore.mode.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackBar = remember { LeoSnackBarController(SnackbarHostState(), scope) }

    LaunchedEffect(sessionEvents) {
        sessionEvents.sessionExpired.collect {
            callController.endCall()
            snackBar.showInfo(AppStrings.sessionExpired)
            navController.goTo(AppRoutes.Login)
        }
    }

    LaunchedEffect(callController) {
        callController.notices.collect { notice ->
            if (notice.isError) snackBar.showError(notice.message) else snackBar.showInfo(notice.message)
        }
    }

    val callState by callController.state.collectAsStateWithLifecycle()
    val hasActiveCall = callState.activeCall != null
    LaunchedEffect(hasActiveCall) {
        if (hasActiveCall) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route.orEmpty()
            if (currentRoute.contains("Splash") || currentRoute.contains("Login") || currentRoute.contains("Onboarding")) {
                navController.navigate(AppRoutes.Home) {
                    popUpTo<AppRoutes.Splash> { inclusive = true }
                    launchSingleTop = true
                }
            }
            if (!currentRoute.contains("Call")) {
                navController.navigate(AppRoutes.Call) { launchSingleTop = true }
            }
        }
    }

    LeoTheme(mode = themeMode) {
        val palette = LeoThemeTokens.palette
        CompositionLocalProvider(LocalLeoSnackBar provides snackBar) {
            ProvideLeoLayout {
                val isMobile = LocalLeoLayout.current == LeoLayout.Mobile
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = palette.background,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        AppNavHost(navController)
                        IncomingCallDialog(
                            onAccepted = { navController.navigate(AppRoutes.Call) { launchSingleTop = true } },
                            onPermissionDenied = { snackBar.showInfo(CallStrings.microphonePermissionRequired) },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .then(
                                    if (isMobile) Modifier.statusBarsPadding()
                                    else Modifier.systemBarsPadding().padding(start = 80.dp, end = 20.dp, top = 10.dp),
                                ),
                        )
                        LeoSnackBarHost(
                            hostState = snackBar.hostState,
                            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                        )
                    }
                }
            }
        }
    }
}
