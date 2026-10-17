import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../core/constants/app_strings.dart';
import '../core/theme/theme.dart';
import '../features/chat/presentation/pages/chat_shell_page.dart';
import '../features/landing/presentation/pages/landing_page.dart';
import '../features/auth/presentation/pages/login_page.dart';
import '../features/onboarding/presentation/pages/onboarding_page.dart';
import '../features/splash/presentation/pages/splash_page.dart';

final _router = GoRouter(
  initialLocation: '/splash',
  routes: [
    GoRoute(path: '/splash', builder: (context, state) => const SplashPage()),
    GoRoute(
        path: '/onboarding',
        builder: (context, state) => const OnboardingPage()),
    GoRoute(path: '/', builder: (context, state) => const LandingPage()),
    GoRoute(path: '/login', builder: (context, state) => const LoginPage()),
    GoRoute(path: '/app', builder: (context, state) => const ChatShellPage()),
  ],
);

class LeoChatApp extends StatelessWidget {
  const LeoChatApp({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp.router(
        title: AppStrings.appName,
        debugShowCheckedModeBanner: false,
        theme: LeoTheme.light,
        routerConfig: _router,
      );
}
