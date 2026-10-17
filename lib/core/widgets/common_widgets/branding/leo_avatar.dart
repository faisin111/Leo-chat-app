import 'package:flutter/material.dart';

import '../../../theme/colors.dart';

class LeoAvatar extends StatelessWidget {
  const LeoAvatar({
    super.key,
    required this.radius,
    this.imageUrl,
    this.assetPath,
    this.fallbackIcon = Icons.person,
    this.backgroundColor = const Color(0xFFFFD8C3),
    this.iconColor = LeoColors.primary,
    this.hasOnlineBadge = false,
  });

  final double radius;
  final String? imageUrl;
  final String? assetPath;
  final IconData fallbackIcon;
  final Color backgroundColor;
  final Color iconColor;
  final bool hasOnlineBadge;

  @override
  Widget build(BuildContext context) {
    Widget avatar = CircleAvatar(
      radius: radius,
      backgroundColor: backgroundColor,
      child: ClipOval(
        child: assetPath != null && assetPath!.isNotEmpty
            ? Image.asset(
                assetPath!,
                width: radius * 2,
                height: radius * 2,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) => Icon(
                  fallbackIcon,
                  size: radius * 1.1,
                  color: iconColor,
                ),
              )
            : imageUrl != null && imageUrl!.isNotEmpty
                ? (imageUrl!.startsWith('assets/')
                    ? Image.asset(
                        imageUrl!,
                        width: radius * 2,
                        height: radius * 2,
                        fit: BoxFit.cover,
                        errorBuilder: (context, error, stackTrace) => Icon(
                          fallbackIcon,
                          size: radius * 1.1,
                          color: iconColor,
                        ),
                      )
                    : Image.network(
                        imageUrl!,
                        width: radius * 2,
                        height: radius * 2,
                        fit: BoxFit.cover,
                        errorBuilder: (context, error, stackTrace) => Icon(
                          fallbackIcon,
                          size: radius * 1.1,
                          color: iconColor,
                        ),
                      ))
                : Icon(
                    fallbackIcon,
                    size: radius * 1.1,
                    color: iconColor,
                  ),
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
