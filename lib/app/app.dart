import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../core/constants/app_strings.dart';
import '../core/auth/auth_session_events.dart';
import '../core/theme/theme.dart';
import '../core/theme/theme_mode_notifier.dart';
import '../core/widgets/common_widgets/scrolling/leo_scroll_behavior.dart';
import '../features/ai/presentation/pages/ai_page.dart';
import '../features/auth/presentation/pages/forgot_password_page.dart';
import '../features/auth/presentation/pages/login_page.dart';
import '../features/auth/presentation/pages/register_page.dart';
import '../features/auth/presentation/pages/reset_password_page.dart';
import '../features/calls/presentation/pages/active_call_page.dart';
import '../features/chat/presentation/pages/chat_shell_page.dart';
import '../features/chat/presentation/pages/group_details_page.dart';
import '../features/chat/presentation/pages/new_conversation_page.dart';
import '../features/chat/presentation/pages/new_group_page.dart';
import '../features/chat/presentation/pages/shared_media_page.dart';
import '../features/landing/presentation/pages/landing_page.dart';
import '../features/onboarding/presentation/pages/onboarding_page.dart';
import '../features/settings/presentation/pages/appearance_page.dart';
import '../features/settings/presentation/pages/edit_profile_page.dart';
import '../features/settings/presentation/pages/security_page.dart';
import '../features/settings/presentation/pages/sessions_page.dart';
import '../features/settings/presentation/pages/settings_page.dart';
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
        path: '/forgot-password',
        pageBuilder: (context, state) =>
            _page(state, const ForgotPasswordPage())),
    GoRoute(
        path: '/reset-password',
        pageBuilder: (context, state) =>
            _page(state, const ResetPasswordPage())),
    GoRoute(
        path: '/app',
        pageBuilder: (context, state) => _page(state, const ChatShellPage())),
    GoRoute(
        path: '/ai',
        pageBuilder: (context, state) => _page(state, const AiPage())),
    GoRoute(
        path: '/call',
        pageBuilder: (context, state) => _page(state, const ActiveCallPage())),
    GoRoute(
        path: '/settings',
        pageBuilder: (context, state) => _page(state, const SettingsPage())),
    GoRoute(
        path: '/settings/sessions',
        pageBuilder: (context, state) => _page(state, const SessionsPage())),
    GoRoute(
        path: '/settings/security',
        pageBuilder: (context, state) => _page(state, const SecurityPage())),
    GoRoute(
        path: '/settings/appearance',
        pageBuilder: (context, state) => _page(state, const AppearancePage())),
    GoRoute(
        path: '/edit-profile',
        pageBuilder: (context, state) => _page(state, const EditProfilePage())),
    GoRoute(
        path: '/new-conversation',
        pageBuilder: (context, state) =>
            _page(state, const NewConversationPage())),
    GoRoute(
        path: '/new-group',
        pageBuilder: (context, state) => _page(state, const NewGroupPage())),
    GoRoute(
        path: '/group-details',
        pageBuilder: (context, state) =>
            _page(state, const GroupDetailsPage())),
    GoRoute(
        path: '/shared-media',
        pageBuilder: (context, state) => _page(state, const SharedMediaPage())),
  ],
);
final _scaffoldMessengerKey = GlobalKey<ScaffoldMessengerState>();

CustomTransitionPage<void> _page(GoRouterState state, Widget child) =>
    CustomTransitionPage<void>(
      key: state.pageKey,
      child: child,
      transitionDuration: const Duration(milliseconds: 320),
      reverseTransitionDuration: const Duration(milliseconds: 240),
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        final tween = Tween<double>(begin: 0.94, end: 1.0)
            .chain(CurveTween(curve: Curves.easeOutCubic));
        final fade = Tween<double>(begin: 0.0, end: 1.0)
            .chain(CurveTween(curve: Curves.easeOutCubic));
        return FadeTransition(
          opacity: animation.drive(fade),
          child: ScaleTransition(scale: animation.drive(tween), child: child),
        );
      },
    );

class LeoChatApp extends ConsumerStatefulWidget {
  const LeoChatApp({super.key});

  @override
  ConsumerState<LeoChatApp> createState() => _LeoChatAppState();
}

class _LeoChatAppState extends ConsumerState<LeoChatApp> {
  int? _lastSessionEvent;

  @override
  void initState() {
    super.initState();
    AuthSessionEvents.sessionExpired.addListener(_onSessionExpired);
  }

  @override
  void dispose() {
    AuthSessionEvents.sessionExpired.removeListener(_onSessionExpired);
    super.dispose();
  }

  void _onSessionExpired() {
    final next = AuthSessionEvents.sessionExpired.value;
    if (next != _lastSessionEvent) {
      _lastSessionEvent = next;
      _scaffoldMessengerKey.currentState?.showSnackBar(
        const SnackBar(
          content: Text(
            'Session expired. Please sign in again.',
            style: TextStyle(fontFamily: 'Poppins'),
          ),
          behavior: SnackBarBehavior.floating,
        ),
      );
      _router.go('/login');
    }
  }

  @override
  Widget build(BuildContext context) {
    final themeMode = ref.watch(themeModeNotifierProvider);

    return MaterialApp.router(
      title: AppStrings.appName,
      theme: LeoTheme.light,
      darkTheme: LeoTheme.dark,
      themeMode: themeMode,
      scrollBehavior: const LeoScrollBehavior(),
      routerConfig: _router,
      scaffoldMessengerKey: _scaffoldMessengerKey,
      debugShowCheckedModeBanner: false,
    );
  }
}
