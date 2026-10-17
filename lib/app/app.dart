import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../core/constants/app_strings.dart';
import '../core/theme/theme.dart';
import '../features/chat/presentation/pages/chat_shell_page.dart';

final _router = GoRouter(
  initialLocation: '/app',
  routes: [
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
