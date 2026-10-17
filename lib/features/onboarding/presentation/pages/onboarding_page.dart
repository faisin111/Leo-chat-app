import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_outline_button.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';

class OnboardingPage extends StatefulWidget {
  const OnboardingPage({super.key});

  @override
  State<OnboardingPage> createState() => _OnboardingPageState();
}

class _OnboardingPageState extends State<OnboardingPage> {
  final _controller = PageController();
  int _page = 0;

  static const _slides = [
    (
      AppStrings.onboardingStepOne,
      AppStrings.onboardingTitleOne,
      AppStrings.onboardingBodyOne,
      AppStrings.onboardingBadgeOne,
      0,
    ),
    (
      AppStrings.onboardingStepTwo,
      AppStrings.onboardingTitleTwo,
      AppStrings.onboardingBodyTwo,
      AppStrings.onboardingBadgeTwo,
      1,
    ),
    (
      AppStrings.onboardingStepThree,
      AppStrings.onboardingTitleThree,
      AppStrings.onboardingBodyThree,
      AppStrings.onboardingBadgeThree,
      2,
    ),
  ];

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _finish() => context.go('/login');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: LeoColors.background,
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 16, 24, 8),
              child: Row(children: [
                const LeoLogo(compact: true),
                const Spacer(),
                LeoOutlineButton(label: AppStrings.skip, onPressed: _finish),
              ]),
            ),
            Expanded(
              child: PageView.builder(
                controller: _controller,
                itemCount: _slides.length,
                onPageChanged: (value) => setState(() => _page = value),
                itemBuilder: (context, index) =>
                    _OnboardingSlide(data: _slides[index]),
              ),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
              child: Row(children: [
                Row(
                  children: List.generate(
                    _slides.length,
                    (index) => _Dot(active: index == _page),
                  ),
                ),
                const Spacer(),
                _page == _slides.length - 1
                    ? LeoPrimaryButton(
                        label: AppStrings.startChatting,
                        icon: Icons.arrow_forward_rounded,
                        onPressed: _finish)
                    : LeoPrimaryButton(
                        label: AppStrings.next,
                        icon: Icons.arrow_forward_rounded,
                        onPressed: () => _controller.nextPage(
                            duration: const Duration(milliseconds: 280),
                            curve: Curves.easeOut)),
              ]),
            ),
            const Padding(
              padding: EdgeInsets.fromLTRB(24, 10, 24, 16),
              child: Text(
                AppStrings.onboardingFooter,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 11,
                  color: LeoColors.textMuted,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _OnboardingSlide extends StatelessWidget {
  const _OnboardingSlide({required this.data});
  final (String, String, String, String, int) data;

  @override
  Widget build(BuildContext context) => SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Rich visual illustration card
              _SlideCardPreview(slideIndex: data.$5),
              const SizedBox(height: 24),
              // Extra info step pill
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: LeoColors.primarySoft,
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  data.$1,
                  style: const TextStyle(
                    color: LeoColors.primary,
                    fontSize: 11,
                    letterSpacing: 0.6,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              const SizedBox(height: 14),
              Text(
                data.$2,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 28,
                  height: 1.15,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -0.8,
                  color: LeoColors.textPrimary,
                ),
              ),
              const SizedBox(height: 12),
              ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 420),
                child: Text(
                  data.$3,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    fontSize: 14,
                    height: 1.5,
                    color: LeoColors.textSecondary,
                  ),
                ),
              ),
              const SizedBox(height: 14),
              // Capability highlight chip
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(999),
                  border: Border.all(color: LeoColors.border),
                  boxShadow: const [
                    BoxShadow(
                      color: Color(0x080F172A),
                      blurRadius: 8,
                      offset: Offset(0, 3),
                    ),
                  ],
                ),
                child: Text(
                  data.$4,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: LeoColors.textPrimary,
                  ),
                ),
              ),
            ],
          ),
        ),
      );
}

class _SlideCardPreview extends StatelessWidget {
  const _SlideCardPreview({required this.slideIndex});
  final int slideIndex;

  @override
  Widget build(BuildContext context) {
    if (slideIndex == 0) {
      return _ChatPreviewCard();
    } else if (slideIndex == 1) {
      return _SyncPreviewCard();
    }
    return _ExpressionPreviewCard();
  }
}

class _ChatPreviewCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) => Container(
        height: 230,
        constraints: const BoxConstraints(maxWidth: 400),
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: LeoColors.border),
          boxShadow: const [
            BoxShadow(
              color: Color(0x120F172A),
              blurRadius: 28,
              offset: Offset(0, 10),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(children: [
              const CircleAvatar(
                radius: 16,
                backgroundColor: LeoColors.primarySoft,
                child: Icon(Icons.person, color: LeoColors.primary, size: 20),
              ),
              const SizedBox(width: 10),
              const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Sarah Jenkins',
                      style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w700,
                          color: LeoColors.textPrimary)),
                  Row(children: [
                    Icon(Icons.circle, size: 7, color: LeoColors.success),
                    SizedBox(width: 4),
                    Text('Active now',
                        style: TextStyle(
                            fontSize: 10, color: LeoColors.textMuted)),
                  ]),
                ],
              ),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: LeoColors.primarySoft,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Text('STOMP',
                    style: TextStyle(
                        fontSize: 9,
                        color: LeoColors.primary,
                        fontWeight: FontWeight.w800)),
              ),
            ]),
            const Spacer(),
            // Incoming bubble
            Align(
              alignment: Alignment.centerLeft,
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 14, vertical: 9),
                decoration: BoxDecoration(
                  color: LeoColors.incomingBubble,
                  borderRadius: BorderRadius.circular(14),
                ),
                child: const Text(
                  'Coffee before the demo? ☕',
                  style: TextStyle(fontSize: 12, color: LeoColors.textPrimary),
                ),
              ),
            ),
            const SizedBox(height: 8),
            // Outgoing bubble
            Align(
              alignment: Alignment.centerRight,
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                decoration: BoxDecoration(
                  color: LeoColors.primary,
                  borderRadius: BorderRadius.circular(14),
                ),
                child: const Wrap(
                  crossAxisAlignment: WrapCrossAlignment.center,
                  spacing: 6,
                  children: [
                    Text(
                      'Yes! 10:30 works ✨',
                      style: TextStyle(fontSize: 12, color: Colors.white),
                    ),
                    Icon(Icons.done_all, size: 14, color: Colors.white),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 6),
            // Reaction chips
            const Align(
              alignment: Alignment.centerRight,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _ReactionChip(emoji: '❤️', count: '3'),
                  SizedBox(width: 5),
                  _ReactionChip(emoji: '🔥', count: '2'),
                ],
              ),
            ),
          ],
        ),
      );
}

class _SyncPreviewCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) => Container(
        height: 230,
        constraints: const BoxConstraints(maxWidth: 400),
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: LeoColors.rail,
          borderRadius: BorderRadius.circular(24),
          boxShadow: const [
            BoxShadow(
              color: Color(0x22020617),
              blurRadius: 28,
              offset: Offset(0, 10),
            ),
          ],
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                const _DeviceIconBox(icon: Icons.phone_iphone, label: 'Mobile'),
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: LeoColors.primary.withOpacity(0.2),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.sync_rounded,
                      color: LeoColors.primary, size: 24),
                ),
                const _DeviceIconBox(icon: Icons.laptop_mac, label: 'Desktop'),
              ],
            ),
            const SizedBox(height: 20),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.06),
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: Colors.white.withOpacity(0.1)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.check_circle, color: LeoColors.success, size: 18),
                  SizedBox(width: 10),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Synced & Offline Ready',
                            style: TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                                fontWeight: FontWeight.w700)),
                        Text('Local cache stores messages securely',
                            style: TextStyle(
                                color: LeoColors.textMuted, fontSize: 10)),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      );
}

class _DeviceIconBox extends StatelessWidget {
  const _DeviceIconBox({required this.icon, required this.label});
  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) => Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: const Color(0xFF1E293B),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Colors.white12),
            ),
            child: Icon(icon, color: Colors.white, size: 28),
          ),
          const SizedBox(height: 6),
          Text(label,
              style: const TextStyle(color: LeoColors.textMuted, fontSize: 11)),
        ],
      );
}

class _ExpressionPreviewCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) => Container(
        height: 230,
        constraints: const BoxConstraints(maxWidth: 400),
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: LeoColors.border),
          boxShadow: const [
            BoxShadow(
              color: Color(0x120F172A),
              blurRadius: 28,
              offset: Offset(0, 10),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // File attachment pill
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: const Color(0xFFFEE2E2),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: const Color(0xFFFCA5A5)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.picture_as_pdf,
                      color: Color(0xFFB91C1C), size: 20),
                  SizedBox(width: 8),
                  Expanded(
                    child: Text('LeoChat_Architecture.pdf',
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                            fontSize: 11,
                            fontWeight: FontWeight.w700,
                            color: Color(0xFFB91C1C))),
                  ),
                  Text('1.4 MB',
                      style: TextStyle(fontSize: 10, color: Color(0xFFB91C1C))),
                ],
              ),
            ),
            const SizedBox(height: 12),
            // Audio wave bar
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: LeoColors.primarySoft,
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Row(
                children: [
                  Icon(Icons.play_circle_fill,
                      color: LeoColors.primary, size: 22),
                  SizedBox(width: 8),
                  Expanded(
                    child: Text('lıllıılııllıııl 0:24',
                        style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: LeoColors.primary)),
                  ),
                  Text('Voice note',
                      style: TextStyle(
                          fontSize: 10,
                          color: LeoColors.primary,
                          fontWeight: FontWeight.w600)),
                ],
              ),
            ),
            const Spacer(),
            // Emoji reaction row
            const Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _EmojiBubble('👍'),
                _EmojiBubble('❤️'),
                _EmojiBubble('🔥'),
                _EmojiBubble('🎉'),
                _EmojiBubble('🚀'),
              ],
            ),
          ],
        ),
      );
}

class _ReactionChip extends StatelessWidget {
  const _ReactionChip({required this.emoji, required this.count});
  final String emoji;
  final String count;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: LeoColors.border),
          boxShadow: const [
            BoxShadow(color: Color(0x0A000000), blurRadius: 4),
          ],
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(emoji, style: const TextStyle(fontSize: 11)),
            const SizedBox(width: 3),
            Text(count,
                style: const TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                    color: LeoColors.textPrimary)),
          ],
        ),
      );
}

class _EmojiBubble extends StatelessWidget {
  const _EmojiBubble(this.emoji);
  final String emoji;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: LeoColors.background,
          shape: BoxShape.circle,
          border: Border.all(color: LeoColors.border),
        ),
        child: Text(emoji, style: const TextStyle(fontSize: 18)),
      );
}

class _Dot extends StatelessWidget {
  const _Dot({required this.active});
  final bool active;

  @override
  Widget build(BuildContext context) => AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      margin: const EdgeInsets.only(right: 6),
      width: active ? 22 : 7,
      height: 7,
      decoration: BoxDecoration(
          color: active ? LeoColors.primary : LeoColors.border,
          borderRadius: BorderRadius.circular(99)));
}
