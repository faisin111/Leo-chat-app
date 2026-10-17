import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/theme/typography.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../../core/widgets/common_widgets/inputs/leo_auth_field.dart';
import '../providers/auth_notifier.dart';
import '../providers/auth_state.dart';

class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) => const _OptionalProviderScope(
        child: _LoginContent(),
      );
}

class _OptionalProviderScope extends StatelessWidget {
  const _OptionalProviderScope({required this.child});
  final Widget child;

  @override
  Widget build(BuildContext context) {
    try {
      ProviderScope.containerOf(context, listen: false);
      return child;
    } catch (_) {
      return ProviderScope(child: child);
    }
  }
}

class _LoginContent extends ConsumerStatefulWidget {
  const _LoginContent();

  @override
  ConsumerState<_LoginContent> createState() => _LoginContentState();
}

class _LoginContentState extends ConsumerState<_LoginContent> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    final username = _usernameController.text.trim();
    final password = _passwordController.text;

    if (username.isEmpty || password.isEmpty) {
      LeoSnackBar.showError(
        context,
        AppStrings.fillRequiredFields,
      );
      return;
    }

    final result = await ref.read(authNotifierProvider.notifier).login(
          username: username,
          password: password,
        );

    if (!mounted) return;

    result.fold(
      (failure) {
        LeoSnackBar.showError(
          context,
          failure.message,
        );
      },
      (_) {
        LeoSnackBar.showSuccess(
          context,
          AppStrings.loginSuccess,
        );
        _safeNavigate('/app');
      },
    );
  }

  void _safeNavigate(String location) {
    try {
      context.go(location);
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authNotifierProvider);
    final isLoading = authState is AuthLoading;

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(24, 16, 24, 28),
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 480),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const LeoLogo(),
                  const SizedBox(height: 24),
                  const _WelcomeCard(),
                  const SizedBox(height: 28),
                  const Text(
                    AppStrings.loginTitle,
                    style: LeoTypography.mainHeading,
                  ),
                  const SizedBox(height: 10),
                  const Text(
                    AppStrings.loginSubtitle,
                    style: TextStyle(
                      fontSize: 15,
                      height: 1.45,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF78716C),
                    ),
                  ),
                  const SizedBox(height: 26),
                  LeoAuthField(
                    label: AppStrings.username,
                    hint: AppStrings.usernameHint,
                    icon: Icons.person_outline_rounded,
                    controller: _usernameController,
                  ),
                  const SizedBox(height: 18),
                  LeoAuthField(
                    label: AppStrings.password,
                    hint: AppStrings.passwordHint,
                    controller: _passwordController,
                    obscureText: _obscurePassword,
                    suffix: IconButton(
                      tooltip:
                          _obscurePassword ? 'Show password' : 'Hide password',
                      onPressed: () =>
                          setState(() => _obscurePassword = !_obscurePassword),
                      icon: Icon(
                        _obscurePassword
                            ? Icons.visibility_off_outlined
                            : Icons.visibility_outlined,
                        color: const Color(0xFF78716C),
                      ),
                    ),
                  ),
                  const SizedBox(height: 10),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton(
                      style: TextButton.styleFrom(
                        padding: EdgeInsets.zero,
                        minimumSize: Size.zero,
                        tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                      ),
                      onPressed: () {},
                      child: const Text(
                        AppStrings.forgotPassword,
                        style: TextStyle(
                          color: LeoColors.primary,
                          fontSize: 14,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    height: 54,
                    child: LeoPrimaryButton(
                      label: AppStrings.login,
                      icon: Icons.arrow_forward_rounded,
                      isLoading: isLoading,
                      onPressed: _handleLogin,
                    ),
                  ),
                  const SizedBox(height: 26),
                  Center(
                    child: Wrap(
                      alignment: WrapAlignment.center,
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        const Text(
                          '${AppStrings.newToLeoChat} ',
                          style: TextStyle(
                            color: Color(0xFF78716C),
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        TextButton(
                          style: TextButton.styleFrom(
                            padding: EdgeInsets.zero,
                            minimumSize: Size.zero,
                            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                          ),
                          onPressed: () => _safeNavigate('/register'),
                          child: const Text(
                            AppStrings.createAccount,
                            style: TextStyle(
                              color: LeoColors.primary,
                              fontSize: 14,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _WelcomeCard extends StatelessWidget {
  const _WelcomeCard();

  @override
  Widget build(BuildContext context) => Container(
        width: double.infinity,
        clipBehavior: Clip.antiAlias,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(28),
          boxShadow: const [
            BoxShadow(
              color: Color(0x28E64A19),
              blurRadius: 24,
              offset: Offset(0, 10),
            ),
          ],
        ),
        child: AspectRatio(
          aspectRatio: 354 / 218,
          child: SvgPicture.asset(
            'assets/illustrations/connection_illustration.svg',
            fit: BoxFit.cover,
          ),
        ),
      );
}
