import 'package:flutter/material.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';

/// Interactive filter bubble selector bar featuring dynamic bubble scaling,
/// glowing shadow feedback, and unread counter badge.
class LeoFilterBubbleBar extends StatelessWidget {
  const LeoFilterBubbleBar({
    super.key,
    required this.selectedIndex,
    required this.onSelect,
    this.unreadCount = 0,
  });

  final int selectedIndex;
  final ValueChanged<int> onSelect;
  final int unreadCount;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      physics: const BouncingScrollPhysics(),
      child: Row(
        children: [
          _BubblePill(
            label: AppStrings.allChats,
            isSelected: selectedIndex == 0,
            onTap: () => onSelect(0),
          ),
          const SizedBox(width: 8),
          _BubblePill(
            label: AppStrings.unread,
            isSelected: selectedIndex == 1,
            badgeCount: unreadCount > 0 ? '$unreadCount' : null,
            onTap: () => onSelect(1),
          ),
          const SizedBox(width: 8),
          _BubblePill(
            label: AppStrings.groups,
            isSelected: selectedIndex == 2,
            onTap: () => onSelect(2),
          ),
        ],
      ),
    );
  }
}

class _BubblePill extends StatelessWidget {
  const _BubblePill({
    required this.label,
    required this.isSelected,
    required this.onTap,
    this.badgeCount,
  });

  final String label;
  final bool isSelected;
  final VoidCallback onTap;
  final String? badgeCount;

  @override
  Widget build(BuildContext context) {
    return AnimatedScale(
      scale: isSelected ? 1.04 : 1.0,
      duration: const Duration(milliseconds: 240),
      curve: Curves.easeOutBack,
      child: LeoPressable(
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            onTap: onTap,
            borderRadius: BorderRadius.circular(999),
            splashColor: const Color(0x22E64A19),
            highlightColor: const Color(0x11E64A19),
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 240),
              curve: Curves.easeOutCubic,
              padding:
                  const EdgeInsets.symmetric(horizontal: 18, vertical: 8.5),
              decoration: BoxDecoration(
                gradient: isSelected
                    ? const LinearGradient(
                        colors: [Color(0xFFE64A19), Color(0xFFFF5722)],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      )
                    : null,
                color: isSelected ? null : const Color(0xFFF3ECE5),
                borderRadius: BorderRadius.circular(999),
                boxShadow: isSelected
                    ? const [
                        BoxShadow(
                          color: Color(0x38E64A19),
                          blurRadius: 10,
                          offset: Offset(0, 4),
                        ),
                      ]
                    : null,
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    label,
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w800,
                      letterSpacing: -0.2,
                      color:
                          isSelected ? Colors.white : const Color(0xFF78716C),
                    ),
                  ),
                  if (badgeCount != null) ...[
                    const SizedBox(width: 6),
                    AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      padding: const EdgeInsets.symmetric(
                          horizontal: 7, vertical: 2),
                      decoration: BoxDecoration(
                        color: isSelected ? Colors.white : LeoColors.primary,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        badgeCount!,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w900,
                          color: isSelected ? LeoColors.primary : Colors.white,
                        ),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
