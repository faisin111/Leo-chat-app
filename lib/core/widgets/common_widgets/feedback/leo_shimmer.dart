import 'dart:io' show Platform;
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

bool get _isInTest {
  if (kIsWeb) return false;
  try {
    return Platform.environment.containsKey('FLUTTER_TEST');
  } catch (_) {
    return false;
  }
}

/// A radiant lightning shimmer effect that sweeps an animated specular beam
/// across child widgets (such as skeletons, avatars, and message bubbles).
class LeoShimmer extends StatefulWidget {
  const LeoShimmer({
    super.key,
    required this.child,
    this.baseColor = const Color(0xFFE8E0D8),
    this.highlightColor = const Color(0xFFF7F2EC),
    this.lightningColor = const Color(0xFFFFFFFF),
    this.duration = const Duration(milliseconds: 1400),
  });

  final Widget child;
  final Color baseColor;
  final Color highlightColor;
  final Color lightningColor;
  final Duration duration;

  @override
  State<LeoShimmer> createState() => _LeoShimmerState();
}

class _LeoShimmerState extends State<LeoShimmer>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: widget.duration,
    );

    if (!_isInTest) {
      _controller.repeat();
    } else {
      _controller.value = 0.5;
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_isInTest) {
      return widget.child;
    }

    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return ShaderMask(
          blendMode: BlendMode.srcATop,
          shaderCallback: (bounds) {
            return LinearGradient(
              begin: const Alignment(-1.3, -0.4),
              end: const Alignment(1.3, 0.4),
              transform: _SlidingGradientTransform(
                slidePercent: _controller.value,
              ),
              colors: [
                widget.baseColor,
                widget.baseColor,
                widget.highlightColor,
                widget.lightningColor, // bright lightning flash
                const Color(0xFFFFF8F2), // radiant warm core
                widget.lightningColor, // second specular spike
                widget.highlightColor,
                widget.baseColor,
                widget.baseColor,
              ],
              stops: const [
                0.0,
                0.28,
                0.42,
                0.48,
                0.50,
                0.52,
                0.58,
                0.72,
                1.0,
              ],
            ).createShader(bounds);
          },
          child: child,
        );
      },
      child: widget.child,
    );
  }
}

class _SlidingGradientTransform extends GradientTransform {
  const _SlidingGradientTransform({required this.slidePercent});

  final double slidePercent;

  @override
  Matrix4? transform(Rect bounds, {TextDirection? textDirection}) {
    // Slide horizontally from -bounds.width to +bounds.width * 2
    final dx = bounds.width * (slidePercent * 2.5 - 1.25);
    return Matrix4.translationValues(dx, 0.0, 0.0);
  }
}
