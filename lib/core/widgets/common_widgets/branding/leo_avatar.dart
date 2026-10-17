import 'package:flutter/material.dart';

import '../../../theme/colors.dart';

class LeoAvatar extends StatelessWidget {
  const LeoAvatar({
    super.key,
    required this.radius,
    this.imageUrl,
    this.assetPath,
    this.fallbackText,
    this.fallbackIcon = Icons.person,
    this.backgroundColor = const Color(0xFFFFD8C3),
    this.iconColor = LeoColors.primary,
    this.hasOnlineBadge = false,
  });

  final double radius;
  final String? imageUrl;
  final String? assetPath;
  final String? fallbackText;
  final IconData fallbackIcon;
  final Color backgroundColor;
  final Color iconColor;
  final bool hasOnlineBadge;

  Widget _fallback() {
    final text = fallbackText?.trim();
    if (text != null && text.isNotEmpty) {
      return Center(
        child: Text(
          text,
          style: TextStyle(
            color: iconColor,
            fontSize: radius * 0.62,
            fontWeight: FontWeight.w800,
          ),
        ),
      );
    }
    return Icon(fallbackIcon, size: radius * 1.1, color: iconColor);
  }

  @override
  Widget build(BuildContext context) {
    Widget avatar = CircleAvatar(
      radius: radius,
      backgroundColor: backgroundColor,
      child: ClipOval(
        child: imageUrl != null && imageUrl!.trim().isNotEmpty
            ? (imageUrl!.startsWith('assets/')
                ? Image.asset(
                    imageUrl!,
                    width: radius * 2,
                    height: radius * 2,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) => _fallback(),
                  )
                : Image.network(
                    imageUrl!,
                    width: radius * 2,
                    height: radius * 2,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) => _fallback(),
                  ))
            : assetPath != null && assetPath!.isNotEmpty
                ? Image.asset(
                    assetPath!,
                    width: radius * 2,
                    height: radius * 2,
                    fit: BoxFit.cover,
                    errorBuilder: (context, error, stackTrace) => _fallback(),
                  )
                : _fallback(),
      ),
    );

    if (hasOnlineBadge) {
      avatar = Stack(
        clipBehavior: Clip.none,
        children: [
          avatar,
          Positioned(
            right: 1,
            bottom: 1,
            child: Container(
              width: radius * 0.38,
              height: radius * 0.38,
              decoration: BoxDecoration(
                color: const Color(0xFF2E8763),
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white, width: 2),
              ),
            ),
          ),
        ],
      );
    }

    return avatar;
  }
}
