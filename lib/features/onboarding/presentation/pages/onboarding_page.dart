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
      AppStrings.onboardingTitleOne,
      AppStrings.onboardingBodyOne,
      Icons.forum_rounded
    ),
    (
      AppStrings.onboardingTitleTwo,
      AppStrings.onboardingBodyTwo,
      Icons.sync_rounded
    ),
    (
      AppStrings.onboardingTitleThree,
      AppStrings.onboardingBodyThree,
      Icons.auto_awesome_rounded
    ),
  ];

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _finish() => context.go('/');

  @override
  Widget build(BuildContext context) {
    final slide = _slides[_page];
    return Scaffold(
      backgroundColor: LeoColors.background,
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
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
              padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
              child: Row(children: [
                Row(
                    children: List.generate(_slides.length,
                        (index) => _Dot(active: index == _page))),
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
          ],
        ),
      ),
    );
  }
}

class _OnboardingSlide extends StatelessWidget {
  const _OnboardingSlide({required this.data});
  final (String, String, IconData) data;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(horizontal: 28),
        child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
          Container(
              width: 210,
              height: 210,
              decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: LeoColors.primarySoft,
                  boxShadow: const [
                    BoxShadow(
                        color: Color(0x1AF35A26),
                        blurRadius: 40,
                        offset: Offset(0, 18))
                  ]),
              child: Center(
                  child: Container(
                      width: 116,
                      height: 116,
                      decoration: BoxDecoration(
                          color: LeoColors.primary,
                          borderRadius: BorderRadius.circular(34)),
                      child: Icon(data.$3, color: Colors.white, size: 54)))),
          const SizedBox(height: 48),
          Text(data.$1,
              textAlign: TextAlign.center,
              style: const TextStyle(
                  fontSize: 30,
                  height: 1.12,
                  fontWeight: FontWeight.w800,
                  letterSpacing: -1,
                  color: LeoColors.textPrimary)),
          const SizedBox(height: 16),
          Text(data.$2,
              textAlign: TextAlign.center,
              style: const TextStyle(
                  fontSize: 15, height: 1.55, color: LeoColors.textSecondary)),
        ]),
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
