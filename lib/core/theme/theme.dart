import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import 'colors.dart';
import 'fields.dart';
import 'typography.dart';

abstract final class LeoTheme {
  static ThemeData get light {
    GoogleFonts.config.allowRuntimeFetching = false;
    return ThemeData(
      useMaterial3: true,
      fontFamily: 'Poppins',
      textTheme: LeoTypography.textTheme,
      scaffoldBackgroundColor: LeoColors.background,
      colorScheme: const ColorScheme.light(
        primary: LeoColors.primary,
        onPrimary: Colors.white,
        surface: LeoColors.surface,
        onSurface: LeoColors.textPrimary,
        error: LeoColors.destructive,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: LeoColors.surface,
        foregroundColor: LeoColors.textPrimary,
        elevation: 0,
        scrolledUnderElevation: 0,
      ),
      inputDecorationTheme: LeoFields.inputDecoration,
    );
  }
}
