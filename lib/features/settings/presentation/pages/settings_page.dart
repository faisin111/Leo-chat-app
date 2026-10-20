import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../auth/presentation/providers/auth_notifier.dart';
import '../../../chat/presentation/providers/chat_notifier.dart';
import '../providers/settings_notifier.dart';
import 'edit_profile_page.dart';

class SettingsPage extends ConsumerWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(settingsNotifierProvider);
    final settingsNotifier = ref.read(settingsNotifierProvider.notifier);

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 32),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Top Header
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Settings',
                    style: TextStyle(
                      fontSize: 28,
                      fontWeight: FontWeight.w900,
                      letterSpacing: -0.8,
                      color: LeoColors.textPrimary,
                    ),
                  ),
                  LeoPressable(
                    child: IconButton(
                      icon: const Icon(
                        Icons.search_rounded,
                        color: LeoColors.textPrimary,
                        size: 26,
                      ),
                      onPressed: () {},
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),

              // Profile Card
              LeoPressable(
                onPressed: () {
                  Navigator.of(context).push(
                    MaterialPageRoute<void>(
                      builder: (_) => const EditProfilePage(),
                    ),
                  );
                },
                child: Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(22),
                    border: Border.all(color: const Color(0xFFE7DFD8)),
                    boxShadow: const [
                      BoxShadow(
                        color: Color(0x0A000000),
                        blurRadius: 10,
                        offset: Offset(0, 4),
                      ),
                    ],
                  ),
                  child: Row(
                    children: [
                      LeoAvatar(
                        radius: 28,
                        imageUrl: settings.avatarUrl,
                        assetPath: settings.avatarUrl,
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              settings.displayName,
                              style: const TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w800,
                                color: LeoColors.textPrimary,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              '@${settings.username} · Edit profile',
                              style: const TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w500,
                                color: LeoColors.textSecondary,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const Icon(
                        Icons.chevron_right_rounded,
                        color: Color(0xFF78716C),
                        size: 24,
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),

              // Section 1: NOTIFICATIONS
              const _SectionHeader(
                icon: Icons.notifications_none_rounded,
                title: 'NOTIFICATIONS',
              ),
              const SizedBox(height: 10),
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: Column(
                  children: [
                    _SettingSwitchTile(
                      title: 'Message notifications',
                      subtitle: 'Stay in the loop with new messages',
                      value: settings.messageNotifications,
                      onChanged: settingsNotifier.toggleMessageNotifications,
                    ),
                    const Divider(
                        height: 1,
                        indent: 18,
                        endIndent: 18,
                        color: Color(0xFFF3ECE5)),
                    _SettingSwitchTile(
                      title: 'Message previews',
                      subtitle: 'Show message content in alerts',
                      value: settings.messagePreviews,
                      onChanged: settingsNotifier.toggleMessagePreviews,
                    ),
                    const Divider(
                        height: 1,
                        indent: 18,
                        endIndent: 18,
                        color: Color(0xFFF3ECE5)),
                    _SettingSwitchTile(
                      title: 'Call notifications',
                      subtitle: 'Get notified when someone calls',
                      value: settings.callNotifications,
                      onChanged: settingsNotifier.toggleCallNotifications,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),

              // Section 2: PRIVACY & ACCOUNT
              const _SectionHeader(
                icon: Icons.shield_outlined,
                title: 'PRIVACY & ACCOUNT',
              ),
              const SizedBox(height: 10),
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: Column(
                  children: [
                    _SettingNavigationTile(
                      title: 'Profile visibility',
                      trailingText: settings.profileVisibility,
                      onTap: () {},
                    ),
                    const Divider(
                        height: 1,
                        indent: 18,
                        endIndent: 18,
                        color: Color(0xFFF3ECE5)),
                    _SettingSwitchTile(
                      title: 'Online status',
                      subtitle: "Let contacts see when you're online",
                      value: settings.onlineStatus,
                      onChanged: settingsNotifier.toggleOnlineStatus,
                    ),
                    const Divider(
                        height: 1,
                        indent: 18,
                        endIndent: 18,
                        color: Color(0xFFF3ECE5)),
                    _SettingNavigationTile(
                      title: 'Blocked people',
                      trailingText: '${settings.blockedCount}',
                      onTap: () {},
                    ),
                    const Divider(
                        height: 1,
                        indent: 18,
                        endIndent: 18,
                        color: Color(0xFFF3ECE5)),
                    _SettingNavigationTile(
                      title: 'Password & security',
                      onTap: () {},
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 28),

              // Bottom links: Help & support, Sign out
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 4),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    LeoPressable(
                      onPressed: () {},
                      child: const Text(
                        'Help & support',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFF78716C),
                        ),
                      ),
                    ),
                    LeoPressable(
                      onPressed: () async {
                        final router = GoRouter.of(context);
                        await ref.read(authNotifierProvider.notifier).logout();
                        ref.read(chatNotifierProvider.notifier).reset();
                        router.go('/login');
                      },
                      child: const Text(
                        'Sign out',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w800,
                          color: Color(0xFFDC2626),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),

              // Footer
              const Center(
                child: Text(
                  'LeoChat · A little closer, every day. · v1.0',
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: Color(0xFFA8A19B),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({required this.icon, required this.title});

  final IconData icon;
  final String title;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, color: LeoColors.primary, size: 18),
        const SizedBox(width: 8),
        Text(
          title,
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w800,
            letterSpacing: 0.6,
            color: Color(0xFF78716C),
          ),
        ),
      ],
    );
  }
}

class _SettingSwitchTile extends StatelessWidget {
  const _SettingSwitchTile({
    required this.title,
    this.subtitle,
    required this.value,
    required this.onChanged,
  });

  final String title;
  final String? subtitle;
  final bool value;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.w700,
                    color: LeoColors.textPrimary,
                  ),
                ),
                if (subtitle != null) ...[
                  const SizedBox(height: 2),
                  Text(
                    subtitle!,
                    style: const TextStyle(
                      fontSize: 12.5,
                      fontWeight: FontWeight.w500,
                      color: Color(0xFF78716C),
                    ),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(width: 12),
          Switch(
            value: value,
            activeColor: Colors.white,
            activeTrackColor: LeoColors.primary,
            inactiveThumbColor: Colors.white,
            inactiveTrackColor: const Color(0xFFD6D3D1),
            materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}

class _SettingNavigationTile extends StatelessWidget {
  const _SettingNavigationTile({
    required this.title,
    this.trailingText,
    required this.onTap,
  });

  final String title;
  final String? trailingText;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(22),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
        child: Row(
          children: [
            Expanded(
              child: Text(
                title,
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w700,
                  color: LeoColors.textPrimary,
                ),
              ),
            ),
            if (trailingText != null) ...[
              Text(
                trailingText!,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFF78716C),
                ),
              ),
              const SizedBox(width: 4),
            ],
            const Icon(
              Icons.chevron_right_rounded,
              color: Color(0xFF78716C),
              size: 20,
            ),
          ],
        ),
      ),
    );
  }
}
