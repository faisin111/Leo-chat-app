import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        backgroundColor: const Color(0xFFFCF9F6),
        body: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(24, 24, 24, 34),
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 520),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const LeoLogo(),
                    const SizedBox(height: 34),
                    const _WelcomePanel(),
                    const SizedBox(height: 34),
                    const Text(AppStrings.loginTitle,
                        style: TextStyle(
                            fontSize: 42,
                            height: 1.1,
                            letterSpacing: -1.5,
                            fontWeight: FontWeight.w800,
                            color: Color(0xFF211D1B))),
                    const SizedBox(height: 14),
                    const Text(AppStrings.loginSubtitle,
                        style: TextStyle(
                            fontSize: 16,
                            height: 1.45,
                            color: Color(0xFF8D827B))),
                    const SizedBox(height: 30),
                    _LoginField(
                      label: AppStrings.username,
                      hint: AppStrings.usernameHint,
                      icon: Icons.person_outline_rounded,
                      controller: _usernameController,
                    ),
                    const SizedBox(height: 18),
                    _LoginField(
                      label: AppStrings.password,
                      hint: AppStrings.passwordHint,
                      icon: Icons.lock_outline_rounded,
                      controller: _passwordController,
                      obscureText: _obscurePassword,
                      suffix: IconButton(
                        tooltip: _obscurePassword
                            ? 'Show password'
                            : 'Hide password',
                        onPressed: () => setState(
                            () => _obscurePassword = !_obscurePassword),
                        icon: Icon(
                            _obscurePassword
                                ? Icons.visibility_off_outlined
                                : Icons.visibility_outlined,
                            color: const Color(0xFF8D827B)),
                      ),
                    ),
                    Align(
                      alignment: Alignment.centerRight,
                      child: TextButton(
                        onPressed: () {},
                        child: const Text(AppStrings.forgotPassword,
                            style: TextStyle(
                                color: LeoColors.primary,
                                fontWeight: FontWeight.w700)),
                      ),
                    ),
                    SizedBox(
                      width: double.infinity,
                      height: 58,
                      child: LeoPrimaryButton(
                          label: AppStrings.login,
                          icon: Icons.arrow_forward_rounded,
                          onPressed: () => context.go('/app')),
                    ),
                    const SizedBox(height: 24),
                    Center(
                      child: Wrap(alignment: WrapAlignment.center, children: [
                        const Text(AppStrings.newToLeoChat,
                            style: TextStyle(
                                color: Color(0xFF8D827B), fontSize: 15)),
                        TextButton(
                            onPressed: () {},
                            child: const Text(AppStrings.createAccount,
                                style: TextStyle(
                                    color: LeoColors.primary,
                                    fontSize: 15,
                                    fontWeight: FontWeight.w700))),
                      ]),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      );
}

class _WelcomePanel extends StatelessWidget {
  const _WelcomePanel();

  @override
  Widget build(BuildContext context) => Container(
        height: 250,
        width: double.infinity,
        clipBehavior: Clip.antiAlias,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(30),
          gradient: const LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [Color(0xFFFF9845), Color(0xFFE72F38)]),
        ),
        child: Stack(children: [
          Positioned(top: -90, right: -45, child: _Arc(size: 250)),
          Positioned(bottom: -135, left: -40, child: _Arc(size: 250)),
          Center(
            child: Column(mainAxisSize: MainAxisSize.min, children: [
              Container(
                margin: const EdgeInsets.symmetric(horizontal: 40),
                padding: const EdgeInsets.fromLTRB(20, 18, 20, 16),
                decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(28)),
                child: const Row(children: [
                  CircleAvatar(
                      radius: 26,
                      backgroundColor: Color(0xFFFFD8C3),
                      child: Icon(Icons.person,
                          color: LeoColors.primary, size: 28)),
                  SizedBox(width: 14),
                  Expanded(
                      child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                        Text('Maya Chen',
                            style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                                color: Color(0xFF211D1B))),
                        SizedBox(height: 5),
                        Text(AppStrings.loginGreeting,
                            style: TextStyle(
                                fontSize: 14, color: Color(0xFF514942))),
                      ])),
                ]),
              ),
              const SizedBox(height: 22),
              Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 26, vertical: 16),
                  decoration: BoxDecoration(
                      color: const Color(0xFF291915),
                      borderRadius: BorderRadius.circular(28)),
                  child: const Text(AppStrings.loginTagline,
                      style: TextStyle(
                          color: Colors.white,
                          fontSize: 17,
                          fontWeight: FontWeight.w700))),
            ]),
          ),
        ]),
      );
}

class _Arc extends StatelessWidget {
  const _Arc({required this.size});
  final double size;
  @override
  Widget build(BuildContext context) => Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: Border.all(color: Colors.white24, width: 1.5)));
}

class _LoginField extends StatelessWidget {
  const _LoginField(
      {required this.label,
      required this.hint,
      required this.icon,
      required this.controller,
      this.obscureText = false,
      this.suffix});
  final String label;
  final String hint;
  final IconData icon;
  final TextEditingController controller;
  final bool obscureText;
  final Widget? suffix;

  @override
  Widget build(BuildContext context) =>
      Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(label,
            style: const TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w700,
                color: Color(0xFF8D827B))),
        const SizedBox(height: 8),
        TextField(
          controller: controller,
          obscureText: obscureText,
          style: const TextStyle(fontSize: 16, color: Color(0xFF211D1B)),
          decoration: InputDecoration(
            hintText: hint,
            hintStyle: const TextStyle(color: Color(0xFFB4AAA3)),
            prefixIcon: Icon(icon, color: const Color(0xFF8D827B)),
            suffixIcon: suffix,
            filled: true,
            fillColor: Colors.white,
            contentPadding:
                const EdgeInsets.symmetric(horizontal: 18, vertical: 18),
            enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(18),
                borderSide:
                    const BorderSide(color: Color(0xFFE9E1DB), width: 1.5)),
            focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(18),
                borderSide:
                    const BorderSide(color: LeoColors.primary, width: 1.5)),
          ),
        ),
      ]);
}
