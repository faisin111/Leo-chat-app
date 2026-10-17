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

/// Reusable skeleton placeholder widget with a warm LeoChat shimmer pulse.
class LeoSkeleton extends StatefulWidget {
  const LeoSkeleton({
    super.key,
    this.width,
    this.height,
    this.borderRadius = 8,
    this.isCircle = false,
  });

  const LeoSkeleton.circle({
    super.key,
    required double size,
  })  : width = size,
        height = size,
        borderRadius = size / 2,
        isCircle = true;

  final double? width;
  final double? height;
  final double borderRadius;
  final bool isCircle;

  @override
  State<LeoSkeleton> createState() => _LeoSkeletonState();
}

class _LeoSkeletonState extends State<LeoSkeleton>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    );

    if (!_isInTest) {
      _controller.repeat(reverse: true);
    } else {
      _controller.value = 0.7;
    }

    _animation = Tween<double>(begin: 0.45, end: 0.95).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        final opacity = _animation.value;
        return Container(
          width: widget.width,
          height: widget.height,
          decoration: BoxDecoration(
            color: Color.lerp(
              const Color(0xFFF3ECE5),
              const Color(0xFFE5DDD5),
              opacity,
            ),
            shape: widget.isCircle ? BoxShape.circle : BoxShape.rectangle,
            borderRadius: widget.isCircle
                ? null
                : BorderRadius.circular(widget.borderRadius),
          ),
        );
      },
    );
  }
}
