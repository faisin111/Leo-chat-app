import 'dart:async';

import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';

class SplashPage extends StatefulWidget {
  const SplashPage({super.key});

  @override
  State<SplashPage> createState() => _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _timer = Timer(const Duration(milliseconds: 900), () {
      if (mounted) context.go('/onboarding');
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        backgroundColor: LeoColors.rail,
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const LeoLogo(onDark: true),
              const SizedBox(height: 30),
              SizedBox(
                width: 52,
                child: LinearProgressIndicator(
                  minHeight: 3,
                  backgroundColor: LeoColors.textSecondary.withOpacity(.35),
                  color: LeoColors.primary,
                  borderRadius: BorderRadius.circular(99),
                ),
              ),
            ],
          ),
        ),
      );
}
