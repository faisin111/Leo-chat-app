import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../../core/widgets/common_widgets/inputs/leo_auth_field.dart';
import '../providers/auth_notifier.dart';
import '../providers/auth_state.dart';

class RegisterPage extends StatelessWidget {
  const RegisterPage({super.key});

  @override
  Widget build(BuildContext context) => const _OptionalProviderScope(
        child: _RegisterContent(),
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

class _RegisterContent extends ConsumerStatefulWidget {
  const _RegisterContent();

  @override
  ConsumerState<_RegisterContent> createState() => _RegisterContentState();
}

class _RegisterContentState extends ConsumerState<_RegisterContent> {
  final _displayName = TextEditingController();
  final _username = TextEditingController();
  final _email = TextEditingController();
  final _password = TextEditingController();
  bool _obscure = true;
  bool _accepted = true;

  @override
  void dispose() {
    _displayName.dispose();
    _username.dispose();
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _handleRegister() async {
    final displayName = _displayName.text.trim();
    final username = _username.text.trim();
    final email = _email.text.trim();
    final password = _password.text;

    if (displayName.isEmpty ||
        username.isEmpty ||
        email.isEmpty ||
        password.isEmpty) {
      LeoSnackBar.showError(context, AppStrings.fillRequiredFields);
      return;
    }

    if (!email.contains('@') || !email.contains('.')) {
      LeoSnackBar.showError(context, AppStrings.invalidEmail);
      return;
    }

    if (password.length < 8) {
      LeoSnackBar.showError(context, AppStrings.passwordTooShort);
      return;
    }

    if (!_accepted) {
      LeoSnackBar.showError(context, AppStrings.acceptTermsRequired);
      return;
    }

    final result = await ref.read(authNotifierProvider.notifier).register(
          username: username,
          email: email,
          password: password,
          displayName: displayName,
        );

    if (!mounted) return;

    result.fold(
      (failure) {
        LeoSnackBar.showError(context, failure.message);
      },
      (_) {
        LeoSnackBar.showSuccess(context, AppStrings.registerSuccess);
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
                  Row(
                    children: [
                      IconButton(
                        tooltip: 'Back to Sign in',
                        padding: EdgeInsets.zero,
                        constraints: const BoxConstraints(),
                        onPressed: () => _safeNavigate('/login'),
                        icon: const Icon(Icons.arrow_back_rounded,
                            size: 26, color: Color(0xFF1C1917)),
                      ),
                      const Spacer(),
                      const LeoLogo(),
                    ],
                  ),
                  const SizedBox(height: 28),
                  const Text(
                    AppStrings.registerTitle,
                    style: TextStyle(
                      fontSize: 38,
                      height: 1.08,
                      letterSpacing: -1.4,
                      fontWeight: FontWeight.w900,
                      color: Color(0xFF1C1917),
                    ),
                  ),
                  const SizedBox(height: 10),
                  const Text(
                    AppStrings.registerSubtitle,
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF78716C),
                    ),
                  ),
                  const SizedBox(height: 26),
                  // Profile photo prompt matching Image 3
                  Row(
                    children: [
                      Stack(
                        children: [
                          const LeoAvatar(
                            radius: 34,
                            assetPath: 'assets/avatars/Portrait.png',
                          ),
                          Positioned(
                            bottom: 0,
                            right: 0,
                            child: Container(
                              padding: const EdgeInsets.all(5),
                              decoration: const BoxDecoration(
                                color: LeoColors.primary,
                                shape: BoxShape.circle,
                              ),
                              child: const Icon(Icons.camera_alt,
                                  color: Colors.white, size: 13),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(width: 16),
                      const Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            AppStrings.profilePhotoPrompt,
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w900,
                              color: Color(0xFF1C1917),
                            ),
                          ),
                          SizedBox(height: 4),
                          Text(
                            AppStrings.profilePhotoSubtext,
                            style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w600,
                              color: Color(0xFF78716C),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),
                  LeoAuthField(
                    label: AppStrings.displayName,
                    hint: AppStrings.displayNameHint,
                    icon: Icons.person_outline_rounded,
                    controller: _displayName,
                  ),
                  const SizedBox(height: 18),
                  LeoAuthField(
                    label: AppStrings.username,
                    hint: AppStrings.usernameHint,
                    icon: Icons.alternate_email_rounded,
                    controller: _username,
                  ),
                  const SizedBox(height: 18),
                  LeoAuthField(
                    label: AppStrings.emailAddress,
                    hint: AppStrings.emailHint,
                    icon: Icons.mail_outline_rounded,
                    controller: _email,
                  ),
                  const SizedBox(height: 18),
                  LeoAuthField(
                    label: AppStrings.createPassword,
                    hint: AppStrings.passwordHint,
                    controller: _password,
                    obscureText: _obscure,
                    suffix: IconButton(
                      tooltip: _obscure ? 'Show password' : 'Hide password',
                      onPressed: () => setState(() => _obscure = !_obscure),
                      icon: Icon(
                        _obscure
                            ? Icons.visibility_off_outlined
                            : Icons.visibility_outlined,
                        color: const Color(0xFF78716C),
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Row(
                    children: [
                      Icon(Icons.check_circle_outline_rounded,
                          color: Color(0xFF2E8763), size: 18),
                      SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          AppStrings.strongPassword,
                          style: TextStyle(
                            color: Color(0xFF2E8763),
                            fontSize: 13,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Checkbox(
                        value: _accepted,
                        activeColor: LeoColors.primary,
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(5)),
                        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                        onChanged: (value) =>
                            setState(() => _accepted = value ?? false),
                      ),
                      const SizedBox(width: 6),
                      const Expanded(
                        child: Padding(
                          padding: EdgeInsets.only(top: 8),
                          child: Text(
                            AppStrings.termsAgreement,
                            style: TextStyle(
                              color: Color(0xFF78716C),
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                              height: 1.35,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    height: 54,
                    child: LeoPrimaryButton(
                      label: 'Create account',
                      icon: Icons.arrow_forward_rounded,
                      isLoading: isLoading,
                      onPressed: _accepted ? _handleRegister : null,
                    ),
                  ),
                  const SizedBox(height: 24),
                  Center(
                    child: Wrap(
                      alignment: WrapAlignment.center,
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        const Text(
                          '${AppStrings.alreadyConversation} ',
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
                          onPressed: () => _safeNavigate('/login'),
                          child: const Text(
                            AppStrings.login,
                            style: TextStyle(
                              color: LeoColors.primary,
                              fontWeight: FontWeight.w800,
                              fontSize: 14,
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
