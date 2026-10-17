import 'package:flutter/material.dart';

import '../../../theme/colors.dart';

class LeoOutlineButton extends StatelessWidget {
  const LeoOutlineButton({super.key, required this.label, this.onPressed});

  final String label;
  final VoidCallback? onPressed;

  @override
  Widget build(BuildContext context) => OutlinedButton(
        onPressed: onPressed,
        style: OutlinedButton.styleFrom(
          foregroundColor: LeoColors.textPrimary,
          side: const BorderSide(color: LeoColors.border),
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
        child: Text(label),
      );
}
