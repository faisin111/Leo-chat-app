import 'package:flutter/material.dart';

import 'colors.dart';

/// Shared input decoration tokens. Keep field geometry consistent across
/// auth and chat flows; feature fields should compose these defaults.
abstract final class LeoFields {
  static const InputDecorationTheme inputDecoration = InputDecorationTheme(
    filled: true,
    fillColor: LeoColors.input,
    border: OutlineInputBorder(
      borderRadius: BorderRadius.all(Radius.circular(16)),
      borderSide: BorderSide.none,
    ),
  );

  static InputDecoration auth({
    required String hint,
    IconData? icon,
    Widget? suffix,
  }) {
    return InputDecoration(
      hintText: hint,
      hintStyle: const TextStyle(
        color: LeoColors.textMuted,
        fontWeight: FontWeight.w500,
        fontSize: 15,
      ),
      suffixIcon: suffix ??
          (icon == null
              ? null
              : Icon(icon, color: LeoColors.textSecondary, size: 22)),
      filled: true,
      fillColor: LeoColors.surface,
      contentPadding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: const BorderSide(color: LeoColors.border, width: 1.2),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: const BorderSide(color: LeoColors.primary, width: 1.5),
      ),
    );
  }
}
