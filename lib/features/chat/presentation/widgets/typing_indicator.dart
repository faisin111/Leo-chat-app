import 'dart:io' show Platform;
import 'dart:math' as math;
import 'package:flutter/material.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';

/// Beautiful incoming typing indicator bubble with 3 animated bouncing dots
/// and test-safe animation controllers.
class LeoTypingIndicator extends StatefulWidget {
  const LeoTypingIndicator({
    super.key,
    this.text,
    this.avatarUrl,
    this.assetPath,
  });

  final String? text;
  final String? avatarUrl;
  final String? assetPath;

  @override
  State<LeoTypingIndicator> createState() => _LeoTypingIndicatorState();
}

class _LeoTypingIndicatorState extends State<LeoTypingIndicator>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  static final bool _isInTest =
      Platform.environment.containsKey('FLUTTER_TEST');

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    );

    if (!_isInTest) {
      _controller.repeat();
    } else {
      _controller.value = 0.5;
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Widget _buildDot(int index) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        // In unit/widget tests, keep a fixed pleasant offset to avoid infinite pump timeout
        final double progress = _isInTest ? (index * 0.25) : _controller.value;
        final double phase = (progress + (index * 0.18)) % 1.0;
        final double bounce = math.sin(phase * 2 * math.pi);
        // Only bounce upward
        final double dy = bounce < 0 ? bounce * 4.5 : 0.0;
        final double opacity = 0.45 + ((-bounce).clamp(0.0, 1.0) * 0.55);

        return Transform.translate(
          offset: Offset(0, dy),
          child: Opacity(
            opacity: _isInTest ? 0.85 : opacity,
            child: Container(
              width: 6.5,
              height: 6.5,
              margin: const EdgeInsets.symmetric(horizontal: 2.2),
              decoration: const BoxDecoration(
                color: LeoColors.primary,
                shape: BoxShape.circle,
              ),
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final statusText = widget.text ?? 'typing...';

    return Align(
      alignment: Alignment.centerLeft,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            LeoAvatar(
              radius: 14,
              imageUrl: widget.avatarUrl,
              assetPath: widget.assetPath ?? 'assets/avatars/Portrait.png',
            ),
            const SizedBox(width: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(20),
                  topRight: Radius.circular(20),
                  bottomRight: Radius.circular(20),
                  bottomLeft: Radius.circular(4),
                ),
                border: Border.all(color: const Color(0xFFEFE8E2)),
                boxShadow: const [
                  BoxShadow(
                    color: Color(0x0D000000),
                    blurRadius: 10,
                    offset: Offset(0, 3),
                  ),
                ],
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    statusText,
                    style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF78716C),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      _buildDot(0),
                      _buildDot(1),
                      _buildDot(2),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
