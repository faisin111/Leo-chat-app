import 'package:flutter/material.dart';

import 'leo_pressable.dart';

class LeoPrimaryButton extends StatelessWidget {
  const LeoPrimaryButton({
    super.key,
    required this.label,
    this.onPressed,
    this.icon,
    this.isLoading = false,
  });

  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    final effectiveOnPressed = isLoading ? null : onPressed;

    final Widget button;
    if (isLoading) {
      button = FilledButton(
        style: FilledButton.styleFrom(
          textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
        ),
        onPressed: null,
        child: const SizedBox(
          width: 22,
          height: 22,
          child: CircularProgressIndicator(
            strokeWidth: 2.5,
            valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
          ),
        ),
      );
    } else if (icon == null) {
      button = FilledButton(
        style: FilledButton.styleFrom(
          textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
        ),
        onPressed: effectiveOnPressed,
        child: Text(label),
      );
    } else {
      button = FilledButton.icon(
        style: FilledButton.styleFrom(
          textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
        ),
        onPressed: effectiveOnPressed,
        icon: Icon(icon),
        label: Text(label),
      );
    }

    return LeoPressable(
      enabled: effectiveOnPressed != null,
      child: button,
    );
  }
}
