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
import com.leochat.features.chat.presentation.controllers.ChatController
import com.leochat.features.chat.presentation.pages.ChatShellPage
import com.leochat.features.chat.presentation.pages.ConversationPage
import com.leochat.features.chat.presentation.pages.GroupDetailsPage
import com.leochat.features.chat.presentation.pages.NewConversationPage
import com.leochat.features.chat.presentation.pages.NewGroupPage
import com.leochat.features.chat.presentation.pages.SharedMediaPage
import com.leochat.features.chat.presentation.pages.ShellNavigator
import com.leochat.features.admin.presentation.pages.AdminDashboardPage
import com.leochat.features.admin.presentation.pages.AdminReportsPage
import com.leochat.features.admin.presentation.pages.AdminUsersPage
import com.leochat.features.calls.presentation.pages.ActiveCallPage
import com.leochat.features.settings.presentation.pages.AppearancePage
import com.leochat.features.settings.presentation.pages.EditProfilePage
import com.leochat.features.settings.presentation.pages.SecurityPage
import com.leochat.features.settings.presentation.pages.SessionsPage
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.remember
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
        composable<AppRoutes.Home> { entry ->
            ChatShellPage(controller = koinViewModel(viewModelStoreOwner = entry), nav = shellNavigator(navController))
        }
        composable<AppRoutes.Conversation> {
            ConversationPage(
                controller = homeChatController(navController, it),
                nav = shellNavigator(navController),
                onBack = { navController.popBackStack(AppRoutes.Home, inclusive = false) },
            )
        }
        composable<AppRoutes.NewConversation> {
            val controller = homeChatController(navController, it)
            NewConversationPage(
                onBack = { navController.popBackStack() },
                onConversationOpened = { conversation ->
                    controller.refresh()
                    controller.selectConversation(conversation)
                    navController.popBackStack(AppRoutes.Home, inclusive = false)
                },
                onNewGroup = { navController.navigate(AppRoutes.NewGroup) },
            )
        }
        composable<AppRoutes.NewGroup> {
            val controller = homeChatController(navController, it)
            NewGroupPage(
                onBack = { navController.popBackStack() },
                onGroupCreated = { group ->
                    controller.refresh()
                    controller.selectConversation(group)
                    navController.popBackStack(AppRoutes.Home, inclusive = false)
                },
            )
        }
        composable<AppRoutes.GroupDetails> {
            val route = it.toRoute<AppRoutes.GroupDetails>()
            GroupDetailsPage(
                conversationId = route.conversationId,
                onBack = { navController.popBackStack() },
                onOpenSharedMedia = { id -> navController.navigate(AppRoutes.SharedMedia(id)) },
            )
        }
        composable<AppRoutes.SharedMedia> {
            val route = it.toRoute<AppRoutes.SharedMedia>()
            SharedMediaPage(conversationId = route.conversationId, onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.Call> {
            ActiveCallPage(onEnded = { navController.popBackStack() })
        }
        composable<AppRoutes.EditProfile> {
            EditProfilePage(onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.Security> {
            SecurityPage(onBack = { navController.popBackStack() }, onSignedOut = { navController.goTo(AppRoutes.Login) })
        }
        composable<AppRoutes.Sessions> {
            SessionsPage(onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.Appearance> {
            AppearancePage(onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.AdminDashboard> {
            AdminDashboardPage(
                onBack = { navController.popBackStack() },
                onUsers = { navController.navigate(AppRoutes.AdminUsers) },
                onReports = { navController.navigate(AppRoutes.AdminReports) },
                onConversations = { navController.popBackStack(AppRoutes.Home, inclusive = false) },
            )
        }
        composable<AppRoutes.AdminUsers> {
            AdminUsersPage(onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.AdminReports> {
            AdminReportsPage(onBack = { navController.popBackStack() })
        }
    }
}

/** Every screen pushed from `/app`. Sign-out clears the stack, which clears the chat controller and disconnects realtime. */
private fun shellNavigator(navController: NavHostController) = ShellNavigator(
    onOpenConversation = { navController.navigate(AppRoutes.Conversation) { launchSingleTop = true } },
    onSignedOut = { navController.goTo(AppRoutes.Login) },
    onNewGroup = { navController.navigate(AppRoutes.NewGroup) },
    onGroupDetails = { id -> navController.navigate(AppRoutes.GroupDetails(id)) },
    onSharedMedia = { id -> navController.navigate(AppRoutes.SharedMedia(id)) },
    onCall = { navController.navigate(AppRoutes.Call) { launchSingleTop = true } },
    onEditProfile = { navController.navigate(AppRoutes.EditProfile) },
    onSecurity = { navController.navigate(AppRoutes.Security) },
    onSessions = { navController.navigate(AppRoutes.Sessions) },
    onAppearance = { navController.navigate(AppRoutes.Appearance) },
    onAdmin = { navController.navigate(AppRoutes.AdminDashboard) },
)

/** The `/app` entry's [ChatController], so pushed screens update the same list, selection, and realtime state. */
@Composable
private fun homeChatController(navController: NavHostController, entry: NavBackStackEntry): ChatController {
    val homeEntry = remember(entry) { navController.getBackStackEntry(AppRoutes.Home) }
    return koinViewModel(viewModelStoreOwner = homeEntry)
}
