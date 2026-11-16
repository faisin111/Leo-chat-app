import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../../core/widgets/common_widgets/inputs/leo_auth_field.dart';
import '../../../auth/presentation/providers/auth_notifier.dart';
import '../../../users/presentation/providers/users_providers.dart';

class SecurityPage extends ConsumerStatefulWidget {
  const SecurityPage({super.key});

  @override
  ConsumerState<SecurityPage> createState() => _SecurityPageState();
}

class _SecurityPageState extends ConsumerState<SecurityPage> {
  final _currentPasswordController = TextEditingController();
  final _newPasswordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  bool _obscureCurrent = true;
  bool _obscureNew = true;
  bool _obscureConfirm = true;
  bool _isUpdating = false;

  @override
  void dispose() {
    _currentPasswordController.dispose();
    _newPasswordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _handleChangePassword() async {
    final current = _currentPasswordController.text;
    final newPass = _newPasswordController.text;
    final confirm = _confirmPasswordController.text;

    if (current.isEmpty || newPass.isEmpty || confirm.isEmpty) {
      LeoSnackBar.showError(context, AppStrings.fillRequiredFields);
      return;
    }

    if (newPass.length < 8) {
      LeoSnackBar.showError(context, AppStrings.passwordTooShort);
      return;
    }

    if (newPass != confirm) {
      LeoSnackBar.showError(context, AppStrings.passwordsDoNotMatch);
      return;
    }

    setState(() => _isUpdating = true);
    final result = await ref.read(authNotifierProvider.notifier).changePassword(
          currentPassword: current,
          newPassword: newPass,
        );

    if (!mounted) return;
    setState(() => _isUpdating = false);

    result.fold(
      (failure) => LeoSnackBar.showError(context, failure.message),
      (_) {
        LeoSnackBar.showSuccess(context, 'Password updated successfully!');
        _currentPasswordController.clear();
        _newPasswordController.clear();
        _confirmPasswordController.clear();
      },
    );
  }

  Future<void> _handleDeactivateAccount() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Deactivate Account?'),
        content: const Text(
          'Your account will be deactivated and you will be signed out immediately.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFEF4444),
              foregroundColor: Colors.white,
            ),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Deactivate'),
          ),
        ],
      ),
    );

    if (confirmed == true && mounted) {
      final res = await ref.read(usersRepositoryProvider).deactivateAccount();
      res.fold(
        (f) => LeoSnackBar.showError(context, f.message),
        (_) {
          ref.read(authNotifierProvider.notifier).logout();
          context.go('/login');
        },
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        scrolledUnderElevation: 1,
        leading: LeoPressable(
          child: IconButton(
            icon: const Icon(Icons.arrow_back_rounded,
                color: LeoColors.textPrimary),
            onPressed: () => context.pop(),
          ),
        ),
        title: const Text(
          AppStrings.security,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w800,
            color: LeoColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Change Password Card
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: LeoColors.border),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 40,
                          height: 40,
                          decoration: BoxDecoration(
                            color: LeoColors.primarySoft,
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: const Icon(Icons.key_rounded,
                              color: LeoColors.primary, size: 22),
                        ),
                        const SizedBox(width: 12),
                        const Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Change Password',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                                color: LeoColors.textPrimary,
                              ),
                            ),
                            Text(
                              'Update your password regularly to stay secure',
                              style: TextStyle(
                                fontSize: 12,
                                color: LeoColors.textSecondary,
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    LeoAuthField(
                      label: AppStrings.currentPassword,
                      hint: 'Enter your current password',
                      controller: _currentPasswordController,
                      obscureText: _obscureCurrent,
                      suffix: IconButton(
                        icon: Icon(
                          _obscureCurrent
                              ? Icons.visibility_off_outlined
                              : Icons.visibility_outlined,
                          color: LeoColors.textSecondary,
                        ),
                        onPressed: () =>
                            setState(() => _obscureCurrent = !_obscureCurrent),
                      ),
                    ),
                    const SizedBox(height: 14),
                    LeoAuthField(
                      label: AppStrings.newPassword,
                      hint: AppStrings.strongPassword,
                      controller: _newPasswordController,
                      obscureText: _obscureNew,
                      suffix: IconButton(
                        icon: Icon(
                          _obscureNew
                              ? Icons.visibility_off_outlined
                              : Icons.visibility_outlined,
                          color: LeoColors.textSecondary,
                        ),
                        onPressed: () =>
                            setState(() => _obscureNew = !_obscureNew),
                      ),
                    ),
                    const SizedBox(height: 14),
                    LeoAuthField(
                      label: AppStrings.confirmPassword,
                      hint: 'Confirm your new password',
                      controller: _confirmPasswordController,
                      obscureText: _obscureConfirm,
                      suffix: IconButton(
                        icon: Icon(
                          _obscureConfirm
                              ? Icons.visibility_off_outlined
                              : Icons.visibility_outlined,
                          color: LeoColors.textSecondary,
                        ),
                        onPressed: () =>
                            setState(() => _obscureConfirm = !_obscureConfirm),
                      ),
                    ),
                    const SizedBox(height: 20),
                    SizedBox(
                      width: double.infinity,
                      height: 50,
                      child: LeoPrimaryButton(
                        label: AppStrings.updatePassword,
                        icon: Icons.check_circle_outline_rounded,
                        isLoading: _isUpdating,
                        onPressed: _handleChangePassword,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Two Factor Authentication Info
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: LeoColors.border),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: const Color(0xFF10B981).withOpacity(0.12),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Icon(Icons.verified_user_rounded,
                          color: Color(0xFF10B981), size: 22),
                    ),
                    const SizedBox(width: 14),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Two-Factor Authentication',
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w700,
                              color: LeoColors.textPrimary,
                            ),
                          ),
                          SizedBox(height: 2),
                          Text(
                            'Secured via stateless Bearer tokens and device session keys.',
                            style: TextStyle(
                              fontSize: 12,
                              color: LeoColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Account Deactivation
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(
                      color: const Color(0xFFEF4444).withOpacity(0.3)),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        color: const Color(0xFFEF4444).withOpacity(0.1),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Icon(Icons.person_remove_rounded,
                          color: Color(0xFFEF4444), size: 22),
                    ),
                    const SizedBox(width: 14),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Deactivate Account',
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w700,
                              color: Color(0xFFEF4444),
                            ),
                          ),
                          SizedBox(height: 2),
                          Text(
                            'Temporarily disable your profile and messages',
                            style: TextStyle(
                              fontSize: 12,
                              color: LeoColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                    TextButton(
                      onPressed: _handleDeactivateAccount,
                      child: const Text(
                        'Deactivate',
                        style: TextStyle(
                          color: Color(0xFFEF4444),
                          fontWeight: FontWeight.w700,
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
    );
  }
}
