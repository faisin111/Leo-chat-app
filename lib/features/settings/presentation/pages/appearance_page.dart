import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/theme/theme_mode_notifier.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';

class AppearancePage extends ConsumerStatefulWidget {
  const AppearancePage({super.key});

  @override
  ConsumerState<AppearancePage> createState() => _AppearancePageState();
}

class _AppearancePageState extends ConsumerState<AppearancePage> {
  Color _selectedAccent = LeoColors.primary;

  static const _accents = [
    LeoColors.primary,
    Color(0xFFF43F5E), // Rose
    Color(0xFF3B82F6), // Sky Blue
    Color(0xFF10B981), // Emerald
    Color(0xFF8B5CF6), // Purple
  ];

  @override
  Widget build(BuildContext context) {
    final themeMode = ref.watch(themeModeNotifierProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final selectedModeString = switch (themeMode) {
      ThemeMode.dark => 'Dark',
      ThemeMode.light => 'Light',
      ThemeMode.system => 'System',
    };

    return Scaffold(
      backgroundColor: isDark ? LeoColors.darkBackground : LeoColors.background,
      appBar: AppBar(
        backgroundColor: isDark ? LeoColors.darkSurface : Colors.white,
        elevation: 0,
        scrolledUnderElevation: 1,
        leading: LeoPressable(
          child: IconButton(
            icon: Icon(
              Icons.arrow_back_rounded,
              color: isDark ? LeoColors.darkTextPrimary : LeoColors.textPrimary,
            ),
            onPressed: () => context.pop(),
          ),
        ),
        title: Text(
          AppStrings.appearance,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w800,
            color: isDark ? LeoColors.darkTextPrimary : LeoColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Theme Mode
              Text(
                'THEME MODE',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 0.6,
                  color: isDark
                      ? LeoColors.darkTextSecondary
                      : LeoColors.textSecondary,
                ),
              ),
              const SizedBox(height: 12),

              Row(
                children: ['Light', 'Dark', 'System'].map((mode) {
                  final isSelected = selectedModeString == mode;
                  IconData icon = Icons.light_mode_rounded;
                  if (mode == 'Dark') icon = Icons.dark_mode_rounded;
                  if (mode == 'System') icon = Icons.brightness_auto_rounded;

                  return Expanded(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 4),
                      child: LeoPressable(
                        onPressed: () {
                          final targetMode = switch (mode) {
                            'Dark' => ThemeMode.dark,
                            'Light' => ThemeMode.light,
                            _ => ThemeMode.system,
                          };
                          ref
                              .read(themeModeNotifierProvider.notifier)
                              .setThemeMode(targetMode);
                        },
                        child: Container(
                          padding: const EdgeInsets.symmetric(vertical: 18),
                          decoration: BoxDecoration(
                            color:
                                isDark ? LeoColors.darkSurface : Colors.white,
                            borderRadius: BorderRadius.circular(18),
                            border: Border.all(
                              color: isSelected
                                  ? _selectedAccent
                                  : (isDark
                                      ? LeoColors.darkBorder
                                      : LeoColors.border),
                              width: isSelected ? 2 : 1,
                            ),
                          ),
                          child: Column(
                            children: [
                              Icon(
                                icon,
                                color: isSelected
                                    ? _selectedAccent
                                    : (isDark
                                        ? LeoColors.darkTextSecondary
                                        : const Color(0xFF64748B)),
                                size: 26,
                              ),
                              const SizedBox(height: 8),
                              Text(
                                mode,
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w700,
                                  color: isSelected
                                      ? _selectedAccent
                                      : (isDark
                                          ? LeoColors.darkTextPrimary
                                          : LeoColors.textPrimary),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
              const SizedBox(height: 24),

              // Accent Color
              Text(
                'ACCENT COLOR',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 0.6,
                  color: isDark
                      ? LeoColors.darkTextSecondary
                      : LeoColors.textSecondary,
                ),
              ),
              const SizedBox(height: 12),

              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: isDark ? LeoColors.darkSurface : Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: isDark ? LeoColors.darkBorder : LeoColors.border,
                  ),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: _accents.map((color) {
                    final isSelected = _selectedAccent == color;
                    return LeoPressable(
                      onPressed: () => setState(() => _selectedAccent = color),
                      child: Container(
                        width: 44,
                        height: 44,
                        decoration: BoxDecoration(
                          color: color,
                          shape: BoxShape.circle,
                          border: isSelected
                              ? Border.all(color: Colors.white, width: 3)
                              : null,
                          boxShadow: [
                            if (isSelected)
                              BoxShadow(
                                color: color.withOpacity(0.5),
                                blurRadius: 10,
                                spreadRadius: 2,
                              ),
                          ],
                        ),
                        child: isSelected
                            ? const Icon(Icons.check,
                                color: Colors.white, size: 20)
                            : null,
                      ),
                    );
                  }).toList(),
                ),
              ),
              const SizedBox(height: 24),

              // Preview Card
              Text(
                'LIVE PREVIEW',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 0.6,
                  color: isDark
                      ? LeoColors.darkTextSecondary
                      : LeoColors.textSecondary,
                ),
              ),
              const SizedBox(height: 12),

              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: isDark ? LeoColors.darkSurface : Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(
                    color: isDark ? LeoColors.darkBorder : LeoColors.border,
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Align(
                      alignment: Alignment.centerRight,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 14, vertical: 10),
                        decoration: BoxDecoration(
                          color: _selectedAccent,
                          borderRadius: BorderRadius.circular(16),
                        ),
                        child: const Text(
                          'Loving this fresh aesthetic! ✨',
                          style: TextStyle(color: Colors.white, fontSize: 13),
                        ),
                      ),
                    ),
                    const SizedBox(height: 10),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 14, vertical: 10),
                        decoration: BoxDecoration(
                          color: isDark
                              ? LeoColors.darkIncomingBubble
                              : const Color(0xFFF1F5F9),
                          borderRadius: BorderRadius.circular(16),
                        ),
                        child: Text(
                          'Clean architecture meets responsive polish.',
                          style: TextStyle(
                            color: isDark
                                ? LeoColors.darkTextPrimary
                                : const Color(0xFF1E293B),
                            fontSize: 13,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
