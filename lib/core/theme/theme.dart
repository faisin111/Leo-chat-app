import 'package:flutter/material.dart';

import 'colors.dart';

abstract final class LeoTheme {
  static ThemeData get light => ThemeData(
        useMaterial3: true,
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
        inputDecorationTheme: const InputDecorationTheme(
          filled: true,
          fillColor: LeoColors.input,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.all(Radius.circular(16)),
            borderSide: BorderSide.none,
          ),
        ),
      );
}
