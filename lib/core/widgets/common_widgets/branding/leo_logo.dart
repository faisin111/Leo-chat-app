import 'package:flutter/material.dart';

import '../../../theme/colors.dart';

class LeoLogo extends StatelessWidget {
  const LeoLogo({super.key, this.compact = false, this.onDark = false});

  final bool compact;
  final bool onDark;

  @override
  Widget build(BuildContext context) {
    final foreground = onDark ? Colors.white : LeoColors.textPrimary;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: compact ? 36 : 42,
          height: compact ? 36 : 42,
          decoration: BoxDecoration(
            color: LeoColors.primary,
            borderRadius: BorderRadius.circular(compact ? 11 : 13),
            boxShadow: const [
              BoxShadow(
                  color: Color(0x26F35A26),
                  blurRadius: 18,
                  offset: Offset(0, 7))
            ],
          ),
          child: Icon(Icons.forum_rounded,
              color: Colors.white, size: compact ? 20 : 24),
        ),
        if (!compact) ...[
          const SizedBox(width: 10),
          Text('LeoChat',
              style: TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.w900,
                  letterSpacing: -0.6,
                  color: foreground)),
        ],
      ],
    );
  }
}
