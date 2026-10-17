import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';
import '../../../../core/widgets/common_widgets/inputs/leo_auth_field.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
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

  @override
  Widget build(BuildContext context) => Scaffold(
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
                          onPressed: () => context.go('/login'),
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
                        fontWeight: FontWeight.w800,
                        color: Color(0xFF1C1917),
                      ),
                    ),
                    const SizedBox(height: 10),
                    const Text(
                      AppStrings.registerSubtitle,
                      style: TextStyle(
                        fontSize: 15,
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
                              imageUrl:
                                  'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&auto=format&fit=crop&q=80',
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
                                fontWeight: FontWeight.w800,
                                color: Color(0xFF1C1917),
                              ),
                            ),
                            SizedBox(height: 4),
                            Text(
                              AppStrings.profilePhotoSubtext,
                              style: TextStyle(
                                fontSize: 13,
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
                              fontWeight: FontWeight.w500,
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
                          materialTapTargetSize:
                              MaterialTapTargetSize.shrinkWrap,
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
                        onPressed: _accepted ? () => context.go('/app') : null,
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
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          TextButton(
                            style: TextButton.styleFrom(
                              padding: EdgeInsets.zero,
                              minimumSize: Size.zero,
                              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                            ),
                            onPressed: () => context.go('/login'),
                            child: const Text(
                              AppStrings.login,
                              style: TextStyle(
                                color: LeoColors.primary,
                                fontWeight: FontWeight.w700,
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
