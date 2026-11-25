package com.leochat.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.leochat.core.navigation.AppRoutes
import com.leochat.feature.auth.presentation.passwordrecovery.ForgotPasswordScreen
import com.leochat.feature.auth.presentation.login.LoginScreen
import com.leochat.feature.auth.presentation.register.RegisterScreen
import com.leochat.feature.auth.presentation.passwordrecovery.ResetPasswordScreen
import com.leochat.feature.chat.presentation.ChatViewModel
import com.leochat.feature.home.presentation.home.HomeScreen
import com.leochat.feature.home.presentation.home.rememberConversationNavigator
import com.leochat.core.navigation.LeoTransitions
import com.leochat.core.navigation.goTo
import com.leochat.feature.chat.presentation.conversation.ConversationScreen
import com.leochat.feature.chat.presentation.groupdetails.GroupDetailsScreen
import com.leochat.feature.chat.presentation.newconversation.NewConversationScreen
import com.leochat.feature.chat.presentation.newgroup.NewGroupScreen
import com.leochat.feature.chat.presentation.sharedmedia.SharedMediaScreen
import com.leochat.feature.home.presentation.home.ShellNavigator
import com.leochat.feature.calls.presentation.activecall.ActiveCallScreen
import com.leochat.feature.settings.presentation.appearance.AppearanceScreen
import com.leochat.feature.settings.presentation.editprofile.EditProfileScreen
import com.leochat.feature.settings.presentation.security.SecurityScreen
import com.leochat.feature.settings.presentation.sessions.SessionsScreen
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.remember
import com.leochat.feature.onboarding.presentation.onboarding.OnboardingScreen
import com.leochat.feature.splash.presentation.splash.SplashScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    val shellNav = remember(navController) { shellNavigator(navController) }
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Splash,
        enterTransition = LeoTransitions.enter,
        exitTransition = LeoTransitions.exit,
        popEnterTransition = LeoTransitions.popEnter,
        popExitTransition = LeoTransitions.popExit,
    ) {
        composable<AppRoutes.Splash> {
            SplashScreen(
                onNavigateHome = { navController.goTo(AppRoutes.Home) },
                onNavigateOnboarding = { navController.goTo(AppRoutes.Onboarding) },
            )
        }
        composable<AppRoutes.Onboarding> {
            OnboardingScreen(onFinish = { navController.goTo(AppRoutes.Login) })
        }
        composable<AppRoutes.Login> {
            LoginScreen(
                onNavigateHome = { navController.goTo(AppRoutes.Home) },
                onForgotPassword = { navController.goTo(AppRoutes.ForgotPassword) },
                onRegister = { navController.goTo(AppRoutes.Register) },
            )
        }
        composable<AppRoutes.Register> {
            RegisterScreen(
                onNavigateHome = { navController.goTo(AppRoutes.Home) },
                onSignIn = { navController.goTo(AppRoutes.Login) },
            )
        }
        composable<AppRoutes.ForgotPassword> {
            ForgotPasswordScreen(
                onBack = { if (!navController.popBackStack()) navController.goTo(AppRoutes.Login) },
                onBackToLogin = { navController.goTo(AppRoutes.Login) },
                onEnterResetCode = { navController.navigate(AppRoutes.ResetPassword) },
            )
        }
        composable<AppRoutes.ResetPassword> {
            ResetPasswordScreen(
                onBack = { if (!navController.popBackStack()) navController.goTo(AppRoutes.Login) },
                onNavigateLogin = { navController.goTo(AppRoutes.Login) },
            )
        }
        composable<AppRoutes.Home> { entry ->
            HomeScreen(controller = koinViewModel(viewModelStoreOwner = entry), nav = shellNav)
        }
        composable<AppRoutes.Conversation> {
            val chatController = homeChatController(navController, it)
            ConversationScreen(
                controller = chatController,
                nav = rememberConversationNavigator(shellNav, chatController = chatController),
                onBack = { navController.popBackStack(AppRoutes.Home, inclusive = false) },
            )
        }
        composable<AppRoutes.NewConversation> {
            val controller = homeChatController(navController, it)
            NewConversationScreen(
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
            NewGroupScreen(
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
            GroupDetailsScreen(
                conversationId = route.conversationId,
                onBack = { navController.popBackStack() },
                onOpenSharedMedia = { id -> navController.navigate(AppRoutes.SharedMedia(id)) },
            )
        }
        composable<AppRoutes.SharedMedia> {
            val route = it.toRoute<AppRoutes.SharedMedia>()
            SharedMediaScreen(conversationId = route.conversationId, onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.Call> {
            ActiveCallScreen(onEnded = { navController.popBackStack() })
        }
        composable<AppRoutes.EditProfile> {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.Security> {
            SecurityScreen(onBack = { navController.popBackStack() }, onSignedOut = { navController.goTo(AppRoutes.Login) })
        }
        composable<AppRoutes.Sessions> {
            SessionsScreen(onBack = { navController.popBackStack() })
        }
        composable<AppRoutes.Appearance> {
            AppearanceScreen(onBack = { navController.popBackStack() })
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
)

/** The `/app` entry's [ChatViewModel], so pushed screens update the same list, selection, and realtime state. */
@Composable
private fun homeChatController(navController: NavHostController, entry: NavBackStackEntry): ChatViewModel {
    val homeEntry = remember(entry) { navController.getBackStackEntry(AppRoutes.Home) }
    return koinViewModel(viewModelStoreOwner = homeEntry)
}
