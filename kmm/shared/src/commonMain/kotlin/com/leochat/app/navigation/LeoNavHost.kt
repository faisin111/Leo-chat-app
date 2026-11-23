package com.leochat.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.leochat.core.constants.AppRoutes
import com.leochat.features.auth.presentation.pages.ForgotPasswordPage
import com.leochat.features.auth.presentation.pages.LoginPage
import com.leochat.features.auth.presentation.pages.RegisterPage
import com.leochat.features.auth.presentation.pages.ResetPasswordPage
import com.leochat.features.chat.presentation.pages.ChatShellPage
import com.leochat.features.onboarding.presentation.pages.OnboardingPage
import com.leochat.features.splash.presentation.pages.SplashPage

/** `go`: replace the whole back stack (go_router `context.go`). */
fun NavHostController.goTo(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun LeoNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Splash,
        enterTransition = LeoTransitions.enter,
        exitTransition = LeoTransitions.exit,
        popEnterTransition = LeoTransitions.popEnter,
        popExitTransition = LeoTransitions.popExit,
    ) {
        composable<AppRoutes.Splash> {
            SplashPage(
                onNavigateHome = { navController.goTo(AppRoutes.Home) },
                onNavigateOnboarding = { navController.goTo(AppRoutes.Onboarding) },
            )
        }
        composable<AppRoutes.Onboarding> {
            OnboardingPage(onFinish = { navController.goTo(AppRoutes.Login) })
        }
        composable<AppRoutes.Login> {
            LoginPage(
                onNavigateHome = { navController.goTo(AppRoutes.Home) },
                onForgotPassword = { navController.goTo(AppRoutes.ForgotPassword) },
                onRegister = { navController.goTo(AppRoutes.Register) },
            )
        }
        composable<AppRoutes.Register> {
            RegisterPage(
                onNavigateHome = { navController.goTo(AppRoutes.Home) },
                onSignIn = { navController.goTo(AppRoutes.Login) },
            )
        }
        composable<AppRoutes.ForgotPassword> {
            ForgotPasswordPage(
                onBack = { if (!navController.popBackStack()) navController.goTo(AppRoutes.Login) },
                onBackToLogin = { navController.goTo(AppRoutes.Login) },
                onEnterResetCode = { navController.navigate(AppRoutes.ResetPassword) },
            )
        }
        composable<AppRoutes.ResetPassword> {
            ResetPasswordPage(
                onBack = { if (!navController.popBackStack()) navController.goTo(AppRoutes.Login) },
                onNavigateLogin = { navController.goTo(AppRoutes.Login) },
            )
        }
        composable<AppRoutes.Home> {
            ChatShellPage(onSignedOut = { navController.goTo(AppRoutes.Login) })
        }
    }
}
