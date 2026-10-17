import 'package:flutter/material.dart';

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

    if (isLoading) {
      return FilledButton(
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
    }

    return icon == null
        ? FilledButton(
            style: FilledButton.styleFrom(
              textStyle:
                  const TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
            ),
            onPressed: effectiveOnPressed,
            child: Text(label),
          )
        : FilledButton.icon(
            style: FilledButton.styleFrom(
              textStyle:
                  const TextStyle(fontWeight: FontWeight.w800, fontSize: 16),
            ),
            onPressed: effectiveOnPressed,
            icon: Icon(icon),
            label: Text(label),
          );
  }
}
