import 'package:flutter/widgets.dart';

enum LeoLayout { mobile, tablet, desktop }

LeoLayout layoutForWidth(double width) {
  if (width < 600) return LeoLayout.mobile;
  if (width < 1024) return LeoLayout.tablet;
  return LeoLayout.desktop;
}

extension LeoLayoutX on BuildContext {
  LeoLayout get leoLayout => layoutForWidth(MediaQuery.sizeOf(this).width);
  bool get isMobile => leoLayout == LeoLayout.mobile;
  bool get isTablet => leoLayout == LeoLayout.tablet;
  bool get isDesktop => leoLayout == LeoLayout.desktop;
}
