import 'package:flutter/material.dart';

import '../../../theme/colors.dart';

enum LeoSnackBarType {
  success,
  error,
  info,
}

class LeoSnackBar {
  LeoSnackBar._();

  static void showSuccess(
    BuildContext context,
    String message, {
    String? title,
  }) {
    show(
      context,
      message: message,
      title: title ?? 'Success',
      type: LeoSnackBarType.success,
    );
  }

  static void showError(
    BuildContext context,
    String message, {
    String? title,
  }) {
    show(
      context,
      message: message,
      title: title ?? 'Error',
      type: LeoSnackBarType.error,
    );
  }

  static void showInfo(
    BuildContext context,
    String message, {
    String? title,
  }) {
    show(
      context,
      message: message,
      title: title ?? 'Notice',
      type: LeoSnackBarType.info,
    );
  }

  static void show(
    BuildContext context, {
    required String message,
    required String title,
    required LeoSnackBarType type,
    Duration duration = const Duration(seconds: 4),
  }) {
    final messenger = ScaffoldMessenger.maybeOf(context);
    if (messenger == null) return;

    messenger.hideCurrentSnackBar();

    final (Color accentColor, IconData iconData) = switch (type) {
      LeoSnackBarType.success => (
          const Color(0xFF34D399),
          Icons.check_circle_rounded
        ),
      LeoSnackBarType.error => (
          const Color(0xFFF87171),
          Icons.error_outline_rounded
        ),
      LeoSnackBarType.info => (LeoColors.primary, Icons.info_outline_rounded),
    };

    messenger.showSnackBar(
      SnackBar(
        behavior: SnackBarBehavior.floating,
        backgroundColor: Colors.transparent,
        elevation: 0,
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        padding: EdgeInsets.zero,
        duration: duration,
        dismissDirection: DismissDirection.horizontal,
        content: Container(
          decoration: BoxDecoration(
            color: const Color(0xFF241B18),
            borderRadius: BorderRadius.circular(18),
            border: Border.all(
              color: accentColor.withOpacity(0.35),
              width: 1.2,
            ),
            boxShadow: const [
              BoxShadow(
                color: Color(0x38000000),
                blurRadius: 22,
                offset: Offset(0, 8),
              ),
            ],
          ),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 13),
          child: Row(
            children: [
              Container(
                width: 38,
                height: 38,
                decoration: BoxDecoration(
                  color: accentColor.withOpacity(0.16),
                  shape: BoxShape.circle,
                ),
                child: Center(
                  child: Icon(
                    iconData,
                    color: accentColor,
                    size: 22,
                  ),
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 14,
                        fontWeight: FontWeight.w800,
                        letterSpacing: -0.2,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      message,
                      style: const TextStyle(
                        color: Color(0xFFE7DFD8),
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        height: 1.35,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              IconButton(
                visualDensity: VisualDensity.compact,
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
                icon: const Icon(
                  Icons.close_rounded,
                  size: 18,
                  color: Color(0xFFA8A19B),
                ),
                onPressed: () => messenger.hideCurrentSnackBar(),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
