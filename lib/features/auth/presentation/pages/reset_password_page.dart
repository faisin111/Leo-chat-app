import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/theme/typography.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../../core/widgets/common_widgets/inputs/leo_auth_field.dart';
import '../providers/auth_notifier.dart';

class ResetPasswordPage extends ConsumerStatefulWidget {
  const ResetPasswordPage({super.key});

  @override
  ConsumerState<ResetPasswordPage> createState() => _ResetPasswordPageState();
}

class _ResetPasswordPageState extends ConsumerState<ResetPasswordPage> {
  final _tokenController = TextEditingController();
  final _newPasswordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  bool _obscureNew = true;
  bool _obscureConfirm = true;
  bool _isLoading = false;

  @override
  void dispose() {
    _tokenController.dispose();
    _newPasswordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _handleSubmit() async {
    final token = _tokenController.text.trim();
    final newPassword = _newPasswordController.text;
    final confirmPassword = _confirmPasswordController.text;

    if (token.isEmpty || newPassword.isEmpty || confirmPassword.isEmpty) {
      LeoSnackBar.showError(context, AppStrings.fillRequiredFields);
      return;
    }

    if (newPassword.length < 8) {
      LeoSnackBar.showError(context, AppStrings.passwordTooShort);
      return;
    }

    if (newPassword != confirmPassword) {
      LeoSnackBar.showError(context, AppStrings.passwordsDoNotMatch);
      return;
    }

    setState(() => _isLoading = true);
    final result = await ref
        .read(authNotifierProvider.notifier)
        .resetPassword(token: token, newPassword: newPassword);

    if (!mounted) return;
    setState(() => _isLoading = false);

    result.fold(
      (failure) {
        LeoSnackBar.showError(context, failure.message);
      },
      (_) {
        LeoSnackBar.showSuccess(
          context,
          'Password has been reset successfully. Please sign in.',
        );
        try {
          context.go('/login');
        } catch (_) {}
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LeoColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_rounded,
              color: LeoColors.textPrimary),
          onPressed: () => context.pop(),
        ),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 440),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 56,
                    height: 56,
                    decoration: BoxDecoration(
                      color: LeoColors.primarySoft,
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: const Icon(
                      Icons.key_rounded,
                      color: LeoColors.primary,
                      size: 30,
                    ),
                  ),
                  const SizedBox(height: 24),
                  const Text(
                    AppStrings.resetPasswordTitle,
                    style: LeoTypography.mainHeading,
                  ),
                  const SizedBox(height: 10),
                  const Text(
                    AppStrings.resetPasswordSubtitle,
                    style: TextStyle(
                      fontSize: 15,
                      height: 1.45,
                      fontWeight: FontWeight.w500,
                      color: LeoColors.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 28),
                  LeoAuthField(
                    label: AppStrings.resetToken,
                    hint: 'Paste your recovery token',
                    icon: Icons.vpn_key_outlined,
                    controller: _tokenController,
                  ),
                  const SizedBox(height: 18),
                  LeoAuthField(
                    label: AppStrings.newPassword,
                    hint: AppStrings.passwordHint,
                    controller: _newPasswordController,
                    obscureText: _obscureNew,
                    suffix: IconButton(
                      onPressed: () =>
                          setState(() => _obscureNew = !_obscureNew),
                      icon: Icon(
                        _obscureNew
                            ? Icons.visibility_off_outlined
                            : Icons.visibility_outlined,
                        color: LeoColors.textSecondary,
                      ),
                    ),
                  ),
                  const SizedBox(height: 18),
                  LeoAuthField(
                    label: AppStrings.confirmPassword,
                    hint: AppStrings.passwordHint,
                    controller: _confirmPasswordController,
                    obscureText: _obscureConfirm,
                    suffix: IconButton(
                      onPressed: () =>
                          setState(() => _obscureConfirm = !_obscureConfirm),
                      icon: Icon(
                        _obscureConfirm
                            ? Icons.visibility_off_outlined
                            : Icons.visibility_outlined,
                        color: LeoColors.textSecondary,
                      ),
                    ),
                  ),
                  const SizedBox(height: 26),
                  SizedBox(
                    width: double.infinity,
                    height: 52,
                    child: LeoPrimaryButton(
                      label: AppStrings.updatePassword,
                      icon: Icons.check_circle_outline_rounded,
                      isLoading: _isLoading,
                      onPressed: _handleSubmit,
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
