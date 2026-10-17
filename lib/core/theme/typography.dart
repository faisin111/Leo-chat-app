import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import 'colors.dart';

/// LeoChat's shared type scale. Keeping the Google Fonts generated theme here
/// preserves the existing app typography while giving features one entry point.
abstract final class LeoTypography {
  static TextTheme get textTheme => GoogleFonts.poppinsTextTheme();

  /// Extra-bold display heading used for primary page titles.
  static const mainHeading = TextStyle(
    fontSize: 38,
    height: 1.08,
    letterSpacing: -1.4,
    fontWeight: FontWeight.w900,
    color: LeoColors.textPrimary,
  );
}
