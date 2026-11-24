import 'package:flutter/material.dart';

abstract final class LeoColors {
  // Brand Orange Accent
  static const primary = Color(0xFFE64A19);
  static const primaryHover = Color(0xFFD84315);
  static const primaryLight = Color(0xFFFF5722);
  static const primarySoft = Color(0xFFFEF0EA);
  static const primarySoftDark = Color(0xFF2B1610);

  // Light Theme Tokens (Matches Web Warm Aesthetic)
  static const background = Color(0xFFFAF7F4);
  static const surface = Color(0xFFFFFFFF);
  static const surfaceSubtle = Color(0xFFFBF9F7);
  static const rail = Color(0xFFFFFFFF);
  static const textPrimary = Color(0xFF1C1917);
  static const textSecondary = Color(0xFF78716C);
  static const textMuted = Color(0xFFA8A19B);
  static const border = Color(0xFFE7DFD8);
  static const borderSubtle = Color(0xFFF0EAE4);
  static const input = Color(0xFFF3ECE5);
  static const incomingBubble = Color(0xFFF3ECE5);

  // Dark Theme Tokens (Matches Web Pure OLED Black Aesthetic)
  static const darkBackground = Color(0xFF000000);
  static const darkSurface = Color(0xFF000000);
  static const darkSurfaceElevated = Color(0xFF101014);
  static const darkRail = Color(0xFF000000);
  static const darkTextPrimary = Color(0xFFFFFFFF);
  static const darkTextSecondary = Color(0xFFA1A1AA);
  static const darkTextMuted = Color(0xFF71717A);
  static const darkBorder = Color(0xFF1E1E22);
  static const darkBorderSubtle = Color(0xFF141417);
  static const darkInput = Color(0xFF0D0D10);
  static const darkIncomingBubble = Color(0xFF121216);

  // Status & Utility
  static const success = Color(0xFF10B981);
  static const destructive = Color(0xFFEF4444);
  static const warning = Color(0xFFF59E0B);
  static const info = Color(0xFF3B82F6);
}
