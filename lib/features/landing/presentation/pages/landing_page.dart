import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/responsive/breakpoints.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_outline_button.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';

class LandingPage extends StatelessWidget {
  const LandingPage({super.key});

  @override
  Widget build(BuildContext context) {
    final isMobile =
        layoutForWidth(MediaQuery.sizeOf(context).width) == LeoLayout.mobile;
    return Scaffold(
      backgroundColor: LeoColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          child: Column(
            children: [
              _LandingNav(isMobile: isMobile),
              _HeroSection(isMobile: isMobile),
              const _FeaturesSection(),
              const _LandingFooter(),
            ],
          ),
        ),
      ),
    );
  }
}

class _LandingNav extends StatelessWidget {
  const _LandingNav({required this.isMobile});
  final bool isMobile;

  @override
  Widget build(BuildContext context) => Padding(
        padding:
            EdgeInsets.symmetric(horizontal: isMobile ? 20 : 48, vertical: 22),
        child: Row(
          children: [
            const LeoLogo(),
            const Spacer(),
            if (!isMobile)
              const Padding(
                padding: EdgeInsets.only(right: 18),
                child: Row(
                  children: [
                    Icon(Icons.circle, size: 8, color: LeoColors.success),
                    SizedBox(width: 7),
                    Text('All systems ready',
                        style: TextStyle(
                            fontSize: 12, color: LeoColors.textSecondary)),
                  ],
                ),
              ),
            LeoOutlineButton(
              label: 'Sign in',
              onPressed: () => context.go('/login'),
            ),
          ],
        ),
      );
}

class _HeroSection extends StatelessWidget {
  const _HeroSection({required this.isMobile});
  final bool isMobile;

  @override
  Widget build(BuildContext context) => Padding(
        padding: EdgeInsets.fromLTRB(
            isMobile ? 20 : 80, isMobile ? 46 : 86, isMobile ? 20 : 80, 84),
        child: isMobile
            ? const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [_HeroCopy(), SizedBox(height: 44), _HeroPreview()])
            : const Row(children: [
                Expanded(child: _HeroCopy()),
                SizedBox(width: 72),
                Expanded(child: _HeroPreview())
              ]),
      );
}

class _HeroCopy extends StatelessWidget {
  const _HeroCopy();

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 11, vertical: 7),
            decoration: BoxDecoration(
                color: LeoColors.primarySoft,
                borderRadius: BorderRadius.circular(999)),
            child: const Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 6,
                children: [
                  Icon(Icons.auto_awesome, size: 14, color: LeoColors.primary),
                  Text(AppStrings.landingEyebrow,
                      style: TextStyle(
                          color: LeoColors.primary,
                          fontSize: 12,
                          fontWeight: FontWeight.w700)),
                ]),
          ),
          const SizedBox(height: 22),
          const Text(AppStrings.landingTitle,
              style: TextStyle(
                  fontSize: 48,
                  height: 1.08,
                  letterSpacing: -1.8,
                  fontWeight: FontWeight.w800,
                  color: LeoColors.textPrimary)),
          const SizedBox(height: 20),
          const Text(AppStrings.landingSubtitle,
              style: TextStyle(
                  fontSize: 16, height: 1.6, color: LeoColors.textSecondary)),
          const SizedBox(height: 30),
          Wrap(spacing: 12, runSpacing: 12, children: [
            LeoPrimaryButton(
                label: AppStrings.getStarted,
                icon: Icons.arrow_forward_rounded,
                onPressed: () => context.go('/login')),
            LeoOutlineButton(
                label: AppStrings.seeHowItWorks,
                onPressed: () => context.go('/login')),
          ]),
        ],
      );
}

class _HeroPreview extends StatelessWidget {
  const _HeroPreview();

  @override
  Widget build(BuildContext context) => Container(
        height: 370,
        decoration: BoxDecoration(
          color: LeoColors.rail,
          borderRadius: BorderRadius.circular(28),
          boxShadow: const [
            BoxShadow(
                color: Color(0x18020617), blurRadius: 35, offset: Offset(0, 18))
          ],
        ),
        child: Stack(children: [
          Positioned(
              top: -60,
              right: -40,
              child:
                  _Glow(size: 190, color: LeoColors.primary.withOpacity(.28))),
          Positioned(
              bottom: -75,
              left: -55,
              child: _Glow(size: 210, color: const Color(0x33475569))),
          Padding(
              padding: const EdgeInsets.all(22),
              child: Column(children: [
                Row(children: [
                  const LeoLogo(compact: true, onDark: true),
                  const SizedBox(width: 12),
                  const Text('Messages',
                      style: TextStyle(
                          color: Colors.white,
                          fontSize: 16,
                          fontWeight: FontWeight.w700)),
                  const Spacer(),
                  const Icon(Icons.more_horiz, color: LeoColors.textMuted)
                ]),
                const SizedBox(height: 24),
                const _PreviewBubble(
                    text: 'Hey, are we still on for tonight?', own: false),
                const SizedBox(height: 12),
                const _PreviewBubble(
                    text: 'Absolutely. I saved you a spot ✨', own: true),
                const SizedBox(height: 12),
                const _PreviewBubble(
                    text: 'Perfect. See you soon!', own: false),
              ])),
        ]),
      );
}

class _Glow extends StatelessWidget {
  const _Glow({required this.size, required this.color});
  final double size;
  final Color color;
  @override
  Widget build(BuildContext context) => Container(
      width: size,
      height: size,
      decoration: BoxDecoration(shape: BoxShape.circle, color: color));
}

class _PreviewBubble extends StatelessWidget {
  const _PreviewBubble({required this.text, required this.own});
  final String text;
  final bool own;
  @override
  Widget build(BuildContext context) => Align(
      alignment: own ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 11),
          decoration: BoxDecoration(
              color: own ? LeoColors.primary : const Color(0xFF1E293B),
              borderRadius: BorderRadius.circular(15)),
          child: Text(text,
              style: const TextStyle(color: Colors.white, fontSize: 12))));
}

class _FeaturesSection extends StatelessWidget {
  const _FeaturesSection();
  @override
  Widget build(BuildContext context) => Container(
      color: LeoColors.surface,
      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 56),
      child: Wrap(
          alignment: WrapAlignment.center,
          spacing: 18,
          runSpacing: 18,
          children: const [
            _FeatureCard(
                icon: Icons.bolt_rounded,
                title: AppStrings.realtimeMessaging,
                description: AppStrings.realtimeMessagingDescription),
            _FeatureCard(
                icon: Icons.shield_outlined,
                title: AppStrings.privateByDesign,
                description: AppStrings.privateByDesignDescription),
            _FeatureCard(
                icon: Icons.favorite_outline_rounded,
                title: AppStrings.madeForPeople,
                description: AppStrings.madeForPeopleDescription),
          ]));
}

class _FeatureCard extends StatelessWidget {
  const _FeatureCard(
      {required this.icon, required this.title, required this.description});
  final IconData icon;
  final String title;
  final String description;
  @override
  Widget build(BuildContext context) => SizedBox(
      width: 300,
      child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                    color: LeoColors.primarySoft,
                    borderRadius: BorderRadius.circular(12)),
                child: Icon(icon, color: LeoColors.primary, size: 20)),
            const SizedBox(width: 12),
            Expanded(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                  Text(title,
                      style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          color: LeoColors.textPrimary)),
                  const SizedBox(height: 6),
                  Text(description,
                      style: const TextStyle(
                          fontSize: 12,
                          height: 1.45,
                          color: LeoColors.textSecondary))
                ]))
          ])));
}

class _LandingFooter extends StatelessWidget {
  const _LandingFooter();
  @override
  Widget build(BuildContext context) => const Padding(
      padding: EdgeInsets.all(28),
      child: Text('© 2026 LeoChat',
          style: TextStyle(fontSize: 12, color: LeoColors.textMuted)));
}
