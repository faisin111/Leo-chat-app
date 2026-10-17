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
              const _MetricsSection(),
              const _FeaturesSection(),
              const _CtaBanner(),
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
  Widget build(BuildContext context) {
    final width = MediaQuery.sizeOf(context).width;
    return Padding(
      padding:
          EdgeInsets.symmetric(horizontal: isMobile ? 20 : 40, vertical: 20),
      child: Row(
        children: [
          const LeoLogo(),
          const Spacer(),
          if (!isMobile && width >= 900)
            Container(
              margin: const EdgeInsets.only(right: 16),
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(999),
                border: Border.all(color: LeoColors.border),
              ),
              child: const Row(
                children: [
                  Icon(Icons.circle, size: 8, color: LeoColors.success),
                  SizedBox(width: 7),
                  Text(
                    AppStrings.landingStatus,
                    style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: LeoColors.textSecondary),
                  ),
                ],
              ),
            ),
          LeoOutlineButton(
            label: AppStrings.login,
            onPressed: () => context.go('/login'),
          ),
        ],
      ),
    );
  }
}

class _HeroSection extends StatelessWidget {
  const _HeroSection({required this.isMobile});
  final bool isMobile;

  @override
  Widget build(BuildContext context) => Padding(
        padding: EdgeInsets.fromLTRB(
            isMobile ? 20 : 48, isMobile ? 32 : 56, isMobile ? 20 : 48, 56),
        child: isMobile
            ? const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [_HeroCopy(), SizedBox(height: 36), _HeroPreview()])
            : const Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Expanded(child: _HeroCopy()),
                  SizedBox(width: 48),
                  Expanded(child: _HeroPreview())
                ],
              ),
      );
}

class _HeroCopy extends StatelessWidget {
  const _HeroCopy();

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
                color: LeoColors.primarySoft,
                borderRadius: BorderRadius.circular(999),
                border: Border.all(color: const Color(0x33F35A26))),
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
          const SizedBox(height: 20),
          const Text(AppStrings.landingTitle,
              style: TextStyle(
                  fontSize: 44,
                  height: 1.08,
                  letterSpacing: -1.6,
                  fontWeight: FontWeight.w800,
                  color: LeoColors.textPrimary)),
          const SizedBox(height: 16),
          const Text(AppStrings.landingSubtitle,
              style: TextStyle(
                  fontSize: 15, height: 1.55, color: LeoColors.textSecondary)),
          const SizedBox(height: 28),
          Wrap(spacing: 12, runSpacing: 12, children: [
            LeoPrimaryButton(
                label: AppStrings.getStarted,
                icon: Icons.arrow_forward_rounded,
                onPressed: () => context.go('/login')),
            LeoOutlineButton(
                label: AppStrings.seeHowItWorks,
                onPressed: () => context.go('/login')),
          ]),
          const SizedBox(height: 20),
          const Row(
            children: [
              Icon(Icons.lock_outline, size: 14, color: LeoColors.textMuted),
              SizedBox(width: 6),
              Expanded(
                child: Text(
                  'TLS 1.3 · 100% Offline Cache · No Ads',
                  style: TextStyle(
                    fontSize: 12,
                    color: LeoColors.textMuted,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            ],
          ),
        ],
      );
}

class _HeroPreview extends StatelessWidget {
  const _HeroPreview();

  @override
  Widget build(BuildContext context) => Container(
        decoration: BoxDecoration(
          color: LeoColors.rail,
          borderRadius: BorderRadius.circular(26),
          border: Border.all(color: Colors.white12),
          boxShadow: const [
            BoxShadow(
                color: Color(0x24020617), blurRadius: 36, offset: Offset(0, 16))
          ],
        ),
        child: Stack(children: [
          Positioned(
              top: -60,
              right: -40,
              child:
                  _Glow(size: 190, color: LeoColors.primary.withOpacity(.25))),
          const Positioned(
              bottom: -75,
              left: -55,
              child: _Glow(size: 200, color: Color(0x33475569))),
          Padding(
            padding: const EdgeInsets.all(18),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Window controls header
                Row(children: [
                  Container(
                      width: 9,
                      height: 9,
                      decoration: const BoxDecoration(
                          color: Color(0xFFEF4444), shape: BoxShape.circle)),
                  const SizedBox(width: 6),
                  Container(
                      width: 9,
                      height: 9,
                      decoration: const BoxDecoration(
                          color: Color(0xFFF59E0B), shape: BoxShape.circle)),
                  const SizedBox(width: 6),
                  Container(
                      width: 9,
                      height: 9,
                      decoration: const BoxDecoration(
                          color: Color(0xFF10B981), shape: BoxShape.circle)),
                  const Spacer(),
                  const LeoLogo(compact: true, onDark: true),
                ]),
                const SizedBox(height: 16),
                // Chat user header
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.06),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: const Row(
                    children: [
                      CircleAvatar(
                        radius: 14,
                        backgroundColor: Color(0xFFFFD8C3),
                        child: Icon(Icons.person,
                            color: LeoColors.primary, size: 16),
                      ),
                      SizedBox(width: 10),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Maya Chen',
                              style: TextStyle(
                                  color: Colors.white,
                                  fontSize: 12,
                                  fontWeight: FontWeight.w700)),
                          Row(
                            children: [
                              Icon(Icons.circle,
                                  size: 6, color: LeoColors.success),
                              SizedBox(width: 4),
                              Text('Active now',
                                  style: TextStyle(
                                      color: LeoColors.textMuted,
                                      fontSize: 10)),
                            ],
                          ),
                        ],
                      ),
                      Spacer(),
                      Icon(Icons.phone_outlined,
                          size: 16, color: LeoColors.textMuted),
                    ],
                  ),
                ),
                const SizedBox(height: 14),
                // Incoming message
                const _PreviewBubble(
                    text: 'Hey Alex! Have you reviewed the new specs?',
                    own: false),
                const SizedBox(height: 8),
                // Outgoing message
                Align(
                  alignment: Alignment.centerRight,
                  child: Container(
                    constraints: const BoxConstraints(maxWidth: 280),
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
                    decoration: BoxDecoration(
                      color: LeoColors.primary,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Yes! Realtime STOMP sync is blazing fast 🚀',
                          style: TextStyle(color: Colors.white, fontSize: 12),
                        ),
                        SizedBox(height: 3),
                        Align(
                          alignment: Alignment.centerRight,
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Text('10:42 AM',
                                  style: TextStyle(
                                      color: Colors.white70, fontSize: 9)),
                              SizedBox(width: 4),
                              Icon(Icons.done_all,
                                  size: 12, color: Colors.white),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                // Typing indicator
                const Row(
                  children: [
                    SizedBox(width: 4),
                    Text(
                      'Maya is typing...',
                      style: TextStyle(
                          color: LeoColors.textMuted,
                          fontSize: 11,
                          fontStyle: FontStyle.italic),
                    ),
                  ],
                ),
              ],
            ),
          ),
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
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          decoration: BoxDecoration(
              color: own ? LeoColors.primary : const Color(0xFF1E293B),
              borderRadius: BorderRadius.circular(14)),
          child: Text(text,
              style: const TextStyle(color: Colors.white, fontSize: 12))));
}

class _MetricsSection extends StatelessWidget {
  const _MetricsSection();

  @override
  Widget build(BuildContext context) => Container(
        color: Colors.white,
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
        child: const Wrap(
          alignment: WrapAlignment.center,
          spacing: 20,
          runSpacing: 16,
          children: [
            _MetricCard(
                value: AppStrings.latencyMetric,
                label: AppStrings.latencyLabel,
                icon: Icons.bolt_rounded),
            _MetricCard(
                value: AppStrings.encryptionMetric,
                label: AppStrings.encryptionLabel,
                icon: Icons.shield_outlined),
            _MetricCard(
                value: AppStrings.offlineMetric,
                label: AppStrings.offlineLabel,
                icon: Icons.offline_pin_outlined),
            _MetricCard(
                value: AppStrings.attachmentMetric,
                label: AppStrings.attachmentLabel,
                icon: Icons.cloud_upload_outlined),
          ],
        ),
      );
}

class _MetricCard extends StatelessWidget {
  const _MetricCard(
      {required this.value, required this.label, required this.icon});
  final String value;
  final String label;
  final IconData icon;

  @override
  Widget build(BuildContext context) => Container(
        width: 155,
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
        decoration: BoxDecoration(
          color: LeoColors.background,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: LeoColors.border),
        ),
        child: Column(
          children: [
            Icon(icon, color: LeoColors.primary, size: 22),
            const SizedBox(height: 6),
            Text(value,
                style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                    color: LeoColors.textPrimary)),
            const SizedBox(height: 3),
            Text(label,
                textAlign: TextAlign.center,
                style: const TextStyle(
                    fontSize: 11, color: LeoColors.textSecondary)),
          ],
        ),
      );
}

class _FeaturesSection extends StatelessWidget {
  const _FeaturesSection();
  @override
  Widget build(BuildContext context) => Container(
      color: LeoColors.background,
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 56),
      child: const Column(
        children: [
          Text('Engineered for Real Connection',
              textAlign: TextAlign.center,
              style: TextStyle(
                  fontSize: 30,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -1,
                  color: LeoColors.textPrimary)),
          SizedBox(height: 8),
          Text(
              'A complete modern messaging stack designed for speed, privacy, and delight.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 14, color: LeoColors.textSecondary)),
          SizedBox(height: 36),
          Wrap(
              alignment: WrapAlignment.center,
              spacing: 18,
              runSpacing: 18,
              children: [
                _FeatureCard(
                    icon: Icons.bolt_rounded,
                    title: AppStrings.realtimeMessaging,
                    description: AppStrings.realtimeMessagingDescription,
                    badge: 'STOMP / WSS'),
                _FeatureCard(
                    icon: Icons.shield_outlined,
                    title: AppStrings.privateByDesign,
                    description: AppStrings.privateByDesignDescription,
                    badge: 'Stateless JWT'),
                _FeatureCard(
                    icon: Icons.favorite_outline_rounded,
                    title: AppStrings.madeForPeople,
                    description: AppStrings.madeForPeopleDescription,
                    badge: 'Rich UX'),
                _FeatureCard(
                    icon: Icons.attachment_rounded,
                    title: 'Media & File Hub',
                    description:
                        'High-speed presigned S3 storage for photos, voice notes, and documents up to 10MB.',
                    badge: '10MB Max'),
              ]),
        ],
      ));
}

class _FeatureCard extends StatelessWidget {
  const _FeatureCard(
      {required this.icon,
      required this.title,
      required this.description,
      required this.badge});
  final IconData icon;
  final String title;
  final String description;
  final String badge;

  @override
  Widget build(BuildContext context) => Container(
      width: 270,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: LeoColors.border),
        boxShadow: const [
          BoxShadow(
              color: Color(0x080F172A), blurRadius: 14, offset: Offset(0, 5)),
        ],
      ),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Container(
                padding: const EdgeInsets.all(9),
                decoration: BoxDecoration(
                    color: LeoColors.primarySoft,
                    borderRadius: BorderRadius.circular(12)),
                child: Icon(icon, color: LeoColors.primary, size: 20)),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
              decoration: BoxDecoration(
                color: LeoColors.background,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: LeoColors.border),
              ),
              child: Text(badge,
                  style: const TextStyle(
                      fontSize: 10,
                      fontWeight: FontWeight.w700,
                      color: LeoColors.textSecondary)),
            ),
          ],
        ),
        const SizedBox(height: 14),
        Text(title,
            style: const TextStyle(
                fontSize: 15,
                fontWeight: FontWeight.w700,
                color: LeoColors.textPrimary)),
        const SizedBox(height: 6),
        Text(description,
            style: const TextStyle(
                fontSize: 12, height: 1.45, color: LeoColors.textSecondary)),
      ]));
}

class _CtaBanner extends StatelessWidget {
  const _CtaBanner();

  @override
  Widget build(BuildContext context) => Container(
        margin: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
        padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 36),
        constraints: const BoxConstraints(maxWidth: 820),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(26),
          gradient: const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF2A1612), Color(0xFF0F172A)],
          ),
          boxShadow: const [
            BoxShadow(
                color: Color(0x1F000000), blurRadius: 26, offset: Offset(0, 8)),
          ],
        ),
        child: Column(
          children: [
            const LeoLogo(compact: true, onDark: true),
            const SizedBox(height: 18),
            const Text(
              'Ready for calmer, closer conversations?',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.w800,
                letterSpacing: -0.6,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Join LeoChat today and experience instant messaging built for real connection.',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: LeoColors.textMuted,
                fontSize: 13,
              ),
            ),
            const SizedBox(height: 20),
            LeoPrimaryButton(
              label: 'Join LeoChat now',
              icon: Icons.arrow_forward_rounded,
              onPressed: () => context.go('/login'),
            ),
          ],
        ),
      );
}

class _LandingFooter extends StatelessWidget {
  const _LandingFooter();
  @override
  Widget build(BuildContext context) => const Padding(
      padding: EdgeInsets.fromLTRB(24, 8, 24, 32),
      child: Column(
        children: [
          Text(AppStrings.landingTrustQuote,
              textAlign: TextAlign.center,
              style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                  color: LeoColors.textSecondary)),
          SizedBox(height: 6),
          Text('© 2026 LeoChat · Built with Flutter & Clean Architecture',
              style: TextStyle(fontSize: 11, color: LeoColors.textMuted)),
        ],
      ));
}
