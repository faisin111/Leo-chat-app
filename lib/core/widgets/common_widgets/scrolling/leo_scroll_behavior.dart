import 'package:flutter/material.dart';

/// Shared scroll behavior for a consistent, low-friction feel on every page.
class LeoScrollBehavior extends MaterialScrollBehavior {
  const LeoScrollBehavior();

  @override
  ScrollPhysics getScrollPhysics(BuildContext context) =>
      const BouncingScrollPhysics(parent: AlwaysScrollableScrollPhysics());

  @override
  Widget buildOverscrollIndicator(
    BuildContext context,
    Widget child,
    ScrollableDetails details,
  ) =>
      child;
}
