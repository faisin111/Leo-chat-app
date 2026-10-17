import 'package:flutter/material.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/responsive/breakpoints.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/branding/leo_logo.dart';

class ChatShellPage extends StatefulWidget {
  const ChatShellPage({super.key});

  @override
  State<ChatShellPage> createState() => _ChatShellPageState();
}

class _ChatShellPageState extends State<ChatShellPage> {
  int _selectedTab = 0;
  int _selectedFilter = 0;

  @override
  Widget build(BuildContext context) {
    final isMobile =
        layoutForWidth(MediaQuery.sizeOf(context).width) == LeoLayout.mobile;

    if (!isMobile) {
      return Scaffold(
        backgroundColor: const Color(0xFFFAF7F4),
        body: Row(
          children: [
            Container(
                width: 72, color: LeoColors.rail, child: const _DesktopRail()),
            SizedBox(
              width: 360,
              child: _ChatsContent(
                selectedFilter: _selectedFilter,
                onSelectFilter: (index) =>
                    setState(() => _selectedFilter = index),
              ),
            ),
            const VerticalDivider(width: 1, color: Color(0xFFE7DFD8)),
            const Expanded(child: _EmptyDetailView()),
          ],
        ),
      );
    }

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      body: SafeArea(
        child: _ChatsContent(
          selectedFilter: _selectedFilter,
          onSelectFilter: (index) => setState(() => _selectedFilter = index),
        ),
      ),
      bottomNavigationBar: _BottomNav(
        selectedIndex: _selectedTab,
        onTap: (index) => setState(() => _selectedTab = index),
      ),
    );
  }
}

class _ChatsContent extends StatelessWidget {
  const _ChatsContent({
    required this.selectedFilter,
    required this.onSelectFilter,
  });

  final int selectedFilter;
  final ValueChanged<int> onSelectFilter;

  @override
  Widget build(BuildContext context) => SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Top Bar: Logo & User Avatar
            const Row(
              children: [
                LeoLogo(),
                Spacer(),
                LeoAvatar(
                  radius: 20,
                  assetPath: 'assets/avatars/Portrait.png',
                ),
              ],
            ),
            const SizedBox(height: 20),
            // Header Section: "Your chats" & Compose FAB
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        AppStrings.yourChats,
                        style: TextStyle(
                          fontSize: 34,
                          height: 1.1,
                          letterSpacing: -1.2,
                          fontWeight: FontWeight.w900,
                          color: Color(0xFF1C1917),
                        ),
                      ),
                      SizedBox(height: 4),
                      Text(
                        AppStrings.yourChatsSubtitle,
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: Color(0xFF78716C),
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: LeoColors.primary,
                    borderRadius: BorderRadius.circular(16),
                    boxShadow: const [
                      BoxShadow(
                        color: Color(0x33E64A19),
                        blurRadius: 14,
                        offset: Offset(0, 6),
                      ),
                    ],
                  ),
                  child: const Icon(
                    Icons.edit_square,
                    color: Colors.white,
                    size: 22,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            // Search Bar
            Container(
              height: 50,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              decoration: BoxDecoration(
                color: const Color(0xFFF3ECE5),
                borderRadius: BorderRadius.circular(16),
              ),
              child: const Row(
                children: [
                  Icon(Icons.search_rounded,
                      color: Color(0xFF78716C), size: 22),
                  SizedBox(width: 12),
                  Expanded(
                    child: TextField(
                      decoration: InputDecoration(
                        hintText: AppStrings.searchPeopleOrMessages,
                        hintStyle: TextStyle(
                          color: Color(0xFF78716C),
                          fontSize: 15,
                          fontWeight: FontWeight.w500,
                        ),
                        border: InputBorder.none,
                        isDense: true,
                        contentPadding: EdgeInsets.zero,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 18),
            // Filter Pills Row (horizontally scrollable)
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _FilterPill(
                    label: AppStrings.allChats,
                    active: selectedFilter == 0,
                    onTap: () => onSelectFilter(0),
                  ),
                  const SizedBox(width: 8),
                  _FilterPill(
                    label: AppStrings.unreadCount,
                    active: selectedFilter == 1,
                    hasBadgeNumber: true,
                    onTap: () => onSelectFilter(1),
                  ),
                  const SizedBox(width: 8),
                  _FilterPill(
                    label: AppStrings.groups,
                    active: selectedFilter == 2,
                    onTap: () => onSelectFilter(2),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            // Conversation List matching Image 1
            const _ConversationItem(
              title: 'Maya Chen',
              subtitle: 'This message was deleted',
              time: '9:40',
              unreadCount: '2',
              imageUrl: 'assets/avatars/Portrait.png',
              hasOnlineBadge: true,
            ),
            const _ConversationItem(
              title: 'Weekend people',
              subtitle: 'Noah: This is the one ☼',
              time: '9:38',
              unreadCount: '4',
              isGradientIcon: true,
              iconData: Icons.wb_sunny_outlined,
            ),
            const _ConversationItem(
              title: 'Noah Williams',
              subtitle: 'typing...',
              time: '9:36',
              isTyping: true,
              imageUrl: 'assets/avatars/Portrait.png',
              hasOnlineBadge: true,
            ),
            const _ConversationItem(
              title: 'Sofia Rivera',
              subtitle: 'You: Sending you the photos now!',
              time: '9:28',
              imageUrl: 'assets/avatars/Portrait.png',
            ),
            const _ConversationItem(
              title: 'Ellie Park',
              subtitle: 'Voice message · 0:18',
              time: 'Yesterday',
              imageUrl: 'assets/avatars/Portrait.png',
              hasMutedSpeaker: true,
            ),
            const _ConversationItem(
              title: 'Studio catch-up',
              subtitle: 'Maya: See you all on Monday ✨',
              time: 'Yesterday',
              isGradientIcon: true,
              iconData: Icons.coffee_rounded,
              hasMutedSpeaker: true,
            ),
            const SizedBox(height: 24),
            // Footer text
            const Center(
              child: Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                alignment: WrapAlignment.center,
                spacing: 8,
                children: [
                  Icon(Icons.favorite_border_rounded,
                      size: 15, color: Color(0xFFA8A19B)),
                  Text(
                    AppStrings.madeForConnection,
                    style: TextStyle(
                      fontSize: 12,
                      color: Color(0xFF78716C),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      );
}

class _FilterPill extends StatelessWidget {
  const _FilterPill({
    required this.label,
    required this.active,
    required this.onTap,
    this.hasBadgeNumber = false,
  });

  final String label;
  final bool active;
  final VoidCallback onTap;
  final bool hasBadgeNumber;

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(999),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 8),
          decoration: BoxDecoration(
            color: active ? LeoColors.primary : const Color(0xFFF3ECE5),
            borderRadius: BorderRadius.circular(999),
          ),
          child: hasBadgeNumber && !active
              ? RichText(
                  text: const TextSpan(
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w800,
                      color: Color(0xFF78716C),
                    ),
                    children: [
                      TextSpan(text: 'Unread '),
                      TextSpan(
                        text: '6',
                        style: TextStyle(color: LeoColors.primary),
                      ),
                    ],
                  ),
                )
              : Text(
                  label,
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w800,
                    color: active ? Colors.white : const Color(0xFF78716C),
                  ),
                ),
        ),
      );
}

class _ConversationItem extends StatelessWidget {
  const _ConversationItem({
    required this.title,
    required this.subtitle,
    required this.time,
    this.unreadCount,
    this.imageUrl,
    this.hasOnlineBadge = false,
    this.isGradientIcon = false,
    this.iconData,
    this.isTyping = false,
    this.hasMutedSpeaker = false,
  });

  final String title;
  final String subtitle;
  final String time;
  final String? unreadCount;
  final String? imageUrl;
  final bool hasOnlineBadge;
  final bool isGradientIcon;
  final IconData? iconData;
  final bool isTyping;
  final bool hasMutedSpeaker;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Row(
          children: [
            if (isGradientIcon && iconData != null)
              Container(
                width: 52,
                height: 52,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [Color(0xFFFF7A1A), Color(0xFFE53926)],
                  ),
                ),
                child: Icon(iconData, color: Colors.white, size: 24),
              )
            else
              LeoAvatar(
                radius: 26,
                imageUrl: imageUrl,
                hasOnlineBadge: hasOnlineBadge,
              ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: Color(0xFF1C1917),
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    subtitle,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: isTyping ? FontWeight.w700 : FontWeight.w500,
                      color: isTyping
                          ? LeoColors.primary
                          : const Color(0xFF78716C),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  time,
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight:
                        unreadCount != null ? FontWeight.w700 : FontWeight.w600,
                    color: unreadCount != null
                        ? LeoColors.primary
                        : const Color(0xFF78716C),
                  ),
                ),
                const SizedBox(height: 4),
                if (unreadCount != null)
                  Container(
                    width: 20,
                    height: 20,
                    decoration: const BoxDecoration(
                      color: LeoColors.primary,
                      shape: BoxShape.circle,
                    ),
                    child: Center(
                      child: Text(
                        unreadCount!,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 11,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ),
                  )
                else if (hasMutedSpeaker)
                  const Icon(
                    Icons.volume_off_rounded,
                    color: Color(0xFFA8A19B),
                    size: 16,
                  )
                else
                  const SizedBox(height: 20),
              ],
            ),
          ],
        ),
      );
}

class _BottomNav extends StatelessWidget {
  const _BottomNav({required this.selectedIndex, required this.onTap});

  final int selectedIndex;
  final ValueChanged<int> onTap;

  @override
  Widget build(BuildContext context) => Container(
        decoration: const BoxDecoration(
          color: Color(0xFFFAF7F4),
          border: Border(
            top: BorderSide(color: Color(0xFFE7DFD8), width: 1),
          ),
        ),
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            _NavItem(
              icon: Icons.chat_bubble_outline_rounded,
              activeIcon: Icons.chat_bubble_rounded,
              label: AppStrings.chats,
              active: selectedIndex == 0,
              onTap: () => onTap(0),
            ),
            _NavItem(
              icon: Icons.phone_outlined,
              activeIcon: Icons.phone_rounded,
              label: AppStrings.calls,
              active: selectedIndex == 1,
              onTap: () => onTap(1),
            ),
            _NavItem(
              icon: Icons.people_outline_rounded,
              activeIcon: Icons.people_rounded,
              label: AppStrings.contacts,
              active: selectedIndex == 2,
              onTap: () => onTap(2),
            ),
            _NavItem(
              icon: Icons.tune_rounded,
              activeIcon: Icons.tune_rounded,
              label: AppStrings.settings,
              active: selectedIndex == 3,
              onTap: () => onTap(3),
            ),
          ],
        ),
      );
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.activeIcon,
    required this.label,
    required this.active,
    required this.onTap,
  });

  final IconData icon;
  final IconData activeIcon;
  final String label;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                active ? activeIcon : icon,
                color: active ? LeoColors.primary : const Color(0xFF78716C),
                size: 24,
              ),
              const SizedBox(height: 3),
              Text(
                label,
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: active ? FontWeight.w800 : FontWeight.w600,
                  color: active ? LeoColors.primary : const Color(0xFF78716C),
                ),
              ),
            ],
          ),
        ),
      );
}

class _DesktopRail extends StatelessWidget {
  const _DesktopRail();

  @override
  Widget build(BuildContext context) => const Column(
        children: [
          SizedBox(height: 24),
          LeoLogo(compact: true, onDark: true),
          Spacer(),
          Icon(Icons.chat_bubble_rounded, color: Colors.white, size: 24),
          SizedBox(height: 28),
          Icon(Icons.phone_outlined, color: Color(0xFF78716C), size: 24),
          SizedBox(height: 28),
          Icon(Icons.people_outline_rounded,
              color: Color(0xFF78716C), size: 24),
          SizedBox(height: 28),
          Icon(Icons.tune_rounded, color: Color(0xFF78716C), size: 24),
          SizedBox(height: 24),
        ],
      );
}

class _EmptyDetailView extends StatelessWidget {
  const _EmptyDetailView();

  @override
  Widget build(BuildContext context) => const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.forum_outlined, size: 48, color: Color(0xFFA8A19B)),
            SizedBox(height: 16),
            Text(
              AppStrings.selectConversation,
              style: TextStyle(
                fontSize: 15,
                color: Color(0xFF78716C),
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      );
}
