import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/theme/typography.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_outline_button.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../../core/widgets/common_widgets/inputs/leo_auth_field.dart';
import '../providers/auth_notifier.dart';

class ForgotPasswordPage extends ConsumerStatefulWidget {
  const ForgotPasswordPage({super.key});

  @override
  ConsumerState<ForgotPasswordPage> createState() => _ForgotPasswordPageState();
}

class _ForgotPasswordPageState extends ConsumerState<ForgotPasswordPage> {
  final _emailController = TextEditingController();
  bool _isLoading = false;
  bool _submitted = false;

  @override
  void dispose() {
    _emailController.dispose();
    super.dispose();
  }

  Future<void> _handleSubmit() async {
    final email = _emailController.text.trim();
    if (email.isEmpty) {
      LeoSnackBar.showError(context, AppStrings.fillRequiredFields);
      return;
    }
    if (!email.contains('@') || !email.contains('.')) {
      LeoSnackBar.showError(context, AppStrings.invalidEmail);
      return;
    }

    setState(() => _isLoading = true);
    final result =
        await ref.read(authNotifierProvider.notifier).forgotPassword(email);

    if (!mounted) return;
    setState(() => _isLoading = false);

    result.fold(
      (failure) {
        LeoSnackBar.showError(context, failure.message);
      },
      (_) {
        setState(() => _submitted = true);
        LeoSnackBar.showSuccess(context, AppStrings.resetInstructionsSent);
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
              child: _submitted ? _buildSuccessView() : _buildFormView(),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildFormView() {
    return Column(
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
            Icons.lock_reset_rounded,
            color: LeoColors.primary,
            size: 30,
          ),
        ),
        const SizedBox(height: 24),
        const Text(
          AppStrings.forgotPasswordTitle,
          style: LeoTypography.mainHeading,
        ),
        const SizedBox(height: 10),
        const Text(
          AppStrings.forgotPasswordSubtitle,
          style: TextStyle(
            fontSize: 15,
            height: 1.45,
            fontWeight: FontWeight.w500,
            color: LeoColors.textSecondary,
          ),
        ),
        const SizedBox(height: 28),
        LeoAuthField(
          label: AppStrings.emailAddress,
          hint: AppStrings.emailHint,
          icon: Icons.mail_outline_rounded,
          controller: _emailController,
          keyboardType: TextInputType.emailAddress,
        ),
        const SizedBox(height: 24),
        SizedBox(
          width: double.infinity,
          height: 52,
          child: LeoPrimaryButton(
            label: AppStrings.sendResetLink,
            icon: Icons.send_rounded,
            isLoading: _isLoading,
            onPressed: _handleSubmit,
          ),
        ),
        const SizedBox(height: 16),
        Center(
          child: TextButton(
            onPressed: () => context.go('/login'),
            child: const Text(
              AppStrings.backToLogin,
              style: TextStyle(
                color: LeoColors.primary,
                fontWeight: FontWeight.w700,
                fontSize: 14,
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildSuccessView() {
    return Column(
      children: [
        Container(
          width: 64,
          height: 64,
          decoration: BoxDecoration(
            color: LeoColors.success.withOpacity(0.12),
            shape: BoxShape.circle,
          ),
          child: const Icon(
            Icons.mark_email_read_rounded,
            color: LeoColors.success,
            size: 34,
          ),
        ),
        const SizedBox(height: 20),
        const Text(
          'Check your email',
          style: LeoTypography.mainHeading,
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 12),
        Text(
          'We have sent password recovery instructions to ${_emailController.text}.',
          style: const TextStyle(
            fontSize: 15,
            height: 1.45,
            color: LeoColors.textSecondary,
          ),
          textAlign: TextAlign.center,
        ),
        const SizedBox(height: 28),
        SizedBox(
          width: double.infinity,
          height: 50,
          child: LeoPrimaryButton(
            label: 'Enter reset code',
            icon: Icons.key_rounded,
            onPressed: () => context.push('/reset-password'),
          ),
        ),
        const SizedBox(height: 12),
        SizedBox(
          width: double.infinity,
          height: 50,
          child: LeoOutlineButton(
            label: AppStrings.backToLogin,
            onPressed: () => context.go('/login'),
          ),
        ),
      ],
    );
  }
}
