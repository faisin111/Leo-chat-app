import 'package:flutter/material.dart';

/// Reusable tactile press/click animation widget that provides a responsive,
/// bouncy scale-down-and-bounce-back effect whenever touched or tapped.
class LeoPressable extends StatefulWidget {
  const LeoPressable({
    super.key,
    required this.child,
    this.onPressed,
    this.enabled = true,
    this.pressedScale = 0.94,
    this.duration = const Duration(milliseconds: 110),
    this.curve = Curves.easeOutBack,
    this.pressCurve = Curves.easeInOutCubic,
    this.behavior = HitTestBehavior.opaque,
  });

  /// The widget to wrap with the tactile press animation.
  final Widget child;

  /// Optional tap callback. If provided, [LeoPressable] acts as the gesture
  /// recognizer for tapping. If omitted (null), [LeoPressable] passively
  /// observes pointer events without blocking child tap handling.
  final VoidCallback? onPressed;

  /// Whether the button is currently interactive.
  final bool enabled;

  /// Target scale factor when pressed down (e.g., 0.94 reduces size by 6%).
  final double pressedScale;

  /// Duration of the scale transition.
  final Duration duration;

  /// Curve used when releasing back to normal scale (springy bounce).
  final Curve curve;

  /// Curve used when pressing down.
  final Curve pressCurve;

  /// Hit test behavior when [onPressed] is provided.
  final HitTestBehavior behavior;

  @override
  State<LeoPressable> createState() => _LeoPressableState();
}

class _LeoPressableState extends State<LeoPressable> {
  bool _isPressed = false;

  bool get _effectiveEnabled =>
      widget.enabled && (widget.onPressed == null || widget.enabled);

  void _handlePressDown() {
    if (_effectiveEnabled && !_isPressed) {
      setState(() => _isPressed = true);
    }
  }

  void _handlePressUp() {
    if (_isPressed) {
      setState(() => _isPressed = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final animatedChild = AnimatedScale(
      scale: (_isPressed && _effectiveEnabled) ? widget.pressedScale : 1.0,
      duration: widget.duration,
      curve: _isPressed ? widget.pressCurve : widget.curve,
      child: widget.child,
    );

    // If an onPressed callback is explicitly provided, handle gestures directly.
    if (widget.onPressed != null) {
      return GestureDetector(
        behavior: widget.behavior,
        onTapDown: (_) => _handlePressDown(),
        onTapUp: (_) => _handlePressUp(),
        onTapCancel: _handlePressUp,
        onTap: widget.enabled ? widget.onPressed : null,
        child: animatedChild,
      );
    }

    // Otherwise, passively observe pointer events so child buttons (FilledButton,
    // OutlinedButton, InkWell) receive their taps and ripples uninterrupted.
    return Listener(
      onPointerDown: (_) => _handlePressDown(),
      onPointerUp: (_) => _handlePressUp(),
      onPointerCancel: (_) => _handlePressUp(),
      onPointerMove: (event) {
        if (_isPressed) {
          final renderBox = context.findRenderObject() as RenderBox?;
          if (renderBox != null && renderBox.hasSize) {
            final size = renderBox.size;
            final pos = event.localPosition;
            if (pos.dx < -12 ||
                pos.dx > size.width + 12 ||
                pos.dy < -12 ||
                pos.dy > size.height + 12) {
              _handlePressUp();
            }
          }
        }
      },
      child: animatedChild,
    );
  }
}
