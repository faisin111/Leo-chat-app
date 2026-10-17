import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
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
        backgroundColor: const Color(0xFFFCF9F6),
        body: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(24, 18, 24, 28),
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 520),
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(children: [
                        IconButton(
                            onPressed: () => context.go('/login'),
                            icon:
                                const Icon(Icons.arrow_back_rounded, size: 30)),
                        const Spacer(),
                        const LeoLogo(),
                      ]),
                      const SizedBox(height: 42),
                      const Text(AppStrings.registerTitle,
                          style: TextStyle(
                              fontSize: 44,
                              height: 1.08,
                              letterSpacing: -1.5,
                              fontWeight: FontWeight.w800,
                              color: Color(0xFF211D1B))),
                      const SizedBox(height: 14),
                      const Text(AppStrings.registerSubtitle,
                          style: TextStyle(
                              fontSize: 16, color: Color(0xFF8D827B))),
                      const SizedBox(height: 32),
                      const _ProfilePhotoPrompt(),
                      const SizedBox(height: 28),
                      LeoAuthField(
                          label: AppStrings.displayName,
                          hint: AppStrings.displayNameHint,
                          icon: Icons.person_outline_rounded,
                          controller: _displayName),
                      const SizedBox(height: 17),
                      LeoAuthField(
                          label: AppStrings.username,
                          hint: AppStrings.usernameHint,
                          icon: Icons.alternate_email_rounded,
                          controller: _username),
                      const SizedBox(height: 17),
                      LeoAuthField(
                          label: AppStrings.emailAddress,
                          hint: AppStrings.emailHint,
                          icon: Icons.mail_outline_rounded,
                          controller: _email),
                      const SizedBox(height: 17),
                      LeoAuthField(
                          label: AppStrings.createPassword,
                          hint: AppStrings.passwordHint,
                          icon: Icons.lock_outline_rounded,
                          controller: _password,
                          obscureText: _obscure,
                          suffix: IconButton(
                              onPressed: () =>
                                  setState(() => _obscure = !_obscure),
                              icon: Icon(
                                  _obscure
                                      ? Icons.visibility_off_outlined
                                      : Icons.visibility_outlined,
                                  color: const Color(0xFF8D827B)))),
                      const SizedBox(height: 14),
                      const Row(children: [
                        Icon(Icons.check_circle_outline,
                            color: Color(0xFF2E8763), size: 22),
                        SizedBox(width: 9),
                        Expanded(
                          child: Text(AppStrings.strongPassword,
                              style: TextStyle(
                                  color: Color(0xFF2E8763), fontSize: 14)),
                        )
                      ]),
                      const SizedBox(height: 20),
                      Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Checkbox(
                                value: _accepted,
                                activeColor: LeoColors.primary,
                                onChanged: (value) =>
                                    setState(() => _accepted = value ?? false)),
                            const Expanded(
                                child: Padding(
                                    padding: EdgeInsets.only(top: 11),
                                    child: Text(AppStrings.termsAgreement,
                                        style: TextStyle(
                                            color: Color(0xFF8D827B),
                                            fontSize: 14,
                                            height: 1.35)))),
                          ]),
                      const SizedBox(height: 12),
                      SizedBox(
                          width: double.infinity,
                          height: 58,
                          child: LeoPrimaryButton(
                              label: 'Create account',
                              icon: Icons.arrow_forward_rounded,
                              onPressed:
                                  _accepted ? () => context.go('/app') : null)),
                      const SizedBox(height: 18),
                      Center(
                          child:
                              Wrap(alignment: WrapAlignment.center, children: [
                        const Text(AppStrings.alreadyConversation,
                            style: TextStyle(
                                color: Color(0xFF8D827B), fontSize: 14)),
                        TextButton(
                            onPressed: () => context.go('/login'),
                            child: const Text(AppStrings.login,
                                style: TextStyle(
                                    color: LeoColors.primary,
                                    fontWeight: FontWeight.w700,
                                    fontSize: 14)))
                      ])),
                    ]),
              ),
            ),
          ),
        ),
      );
}

class _ProfilePhotoPrompt extends StatelessWidget {
  const _ProfilePhotoPrompt();

  @override
  Widget build(BuildContext context) => Row(children: [
        Stack(children: [
          const CircleAvatar(
              radius: 44,
              backgroundColor: Color(0xFFFFE7D8),
              child: Icon(Icons.person, color: LeoColors.primary, size: 48)),
          Positioned(
              bottom: 0,
              right: 0,
              child: Container(
                  padding: const EdgeInsets.all(8),
                  decoration: const BoxDecoration(
                      color: LeoColors.primary, shape: BoxShape.circle),
                  child: const Icon(Icons.camera_alt_outlined,
                      color: Colors.white, size: 17))),
        ]),
        const SizedBox(width: 18),
        const Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Add your best hello',
              style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w800,
                  color: Color(0xFF211D1B))),
          SizedBox(height: 5),
          Text('Profile photo · optional',
              style: TextStyle(color: Color(0xFF8D827B), fontSize: 15))
        ])),
      ]);
}
