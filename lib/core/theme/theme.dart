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
      brightness: Brightness.light,
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
      cardTheme: const CardTheme(
        color: LeoColors.surface,
        elevation: 0,
      ),
      dividerTheme: const DividerThemeData(
        color: LeoColors.border,
        thickness: 1,
      ),
      inputDecorationTheme: LeoFields.inputDecoration,
    );
  }

  static ThemeData get dark {
    GoogleFonts.config.allowRuntimeFetching = false;
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      fontFamily: 'Poppins',
      textTheme: GoogleFonts.poppinsTextTheme(ThemeData.dark().textTheme),
      scaffoldBackgroundColor: LeoColors.darkBackground,
      colorScheme: const ColorScheme.dark(
        primary: LeoColors.primary,
        onPrimary: Colors.white,
        surface: LeoColors.darkSurface,
        onSurface: LeoColors.darkTextPrimary,
        error: LeoColors.destructive,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: LeoColors.darkSurface,
        foregroundColor: LeoColors.darkTextPrimary,
        elevation: 0,
        scrolledUnderElevation: 0,
      ),
      cardTheme: const CardTheme(
        color: LeoColors.darkSurface,
        elevation: 0,
      ),
      dividerTheme: const DividerThemeData(
        color: LeoColors.darkBorder,
        thickness: 1,
      ),
      inputDecorationTheme: LeoFields.darkInputDecoration,
    );
  }
}
