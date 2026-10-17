import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../core/constants/app_strings.dart';
import '../core/theme/theme.dart';
import '../core/widgets/common_widgets/scrolling/leo_scroll_behavior.dart';
import '../features/chat/presentation/pages/chat_shell_page.dart';
import '../features/landing/presentation/pages/landing_page.dart';
import '../features/auth/presentation/pages/login_page.dart';
import '../features/auth/presentation/pages/register_page.dart';
import '../features/onboarding/presentation/pages/onboarding_page.dart';
import '../features/splash/presentation/pages/splash_page.dart';

final _router = GoRouter(
  initialLocation: '/splash',
  routes: [
    GoRoute(
        path: '/splash',
        pageBuilder: (context, state) => _page(state, const SplashPage())),
    GoRoute(
        path: '/onboarding',
        pageBuilder: (context, state) => _page(state, const OnboardingPage())),
    GoRoute(
        path: '/',
        pageBuilder: (context, state) => _page(state, const LandingPage())),
    GoRoute(
        path: '/login',
        pageBuilder: (context, state) => _page(state, const LoginPage())),
    GoRoute(
        path: '/register',
        pageBuilder: (context, state) => _page(state, const RegisterPage())),
    GoRoute(
        path: '/app',
        pageBuilder: (context, state) => _page(state, const ChatShellPage())),
  ],
);

CustomTransitionPage<void> _page(GoRouterState state, Widget child) =>
    CustomTransitionPage<void>(
      key: state.pageKey,
      child: child,
      transitionDuration: const Duration(milliseconds: 320),
      reverseTransitionDuration: const Duration(milliseconds: 240),
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        final curved = CurvedAnimation(
          parent: animation,
          curve: Curves.easeOutCubic,
          reverseCurve: Curves.easeInCubic,
        );
        return FadeTransition(
          opacity: curved,
          child: SlideTransition(
            position: Tween<Offset>(
              begin: const Offset(0.025, 0),
              end: Offset.zero,
            ).animate(curved),
            child: child,
          ),
        );
      },
    );

class LeoChatApp extends StatelessWidget {
  const LeoChatApp({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp.router(
        title: AppStrings.appName,
        debugShowCheckedModeBanner: false,
        theme: LeoTheme.light,
        scrollBehavior: const LeoScrollBehavior(),
        routerConfig: _router,
      );
}
