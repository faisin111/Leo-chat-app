import 'package:flutter/material.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/responsive/breakpoints.dart';
import '../../../../core/theme/colors.dart';

class ChatShellPage extends StatelessWidget {
  const ChatShellPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: LayoutBuilder(
        builder: (context, constraints) {
          if (layoutForWidth(constraints.maxWidth) == LeoLayout.mobile) {
            return const _ConversationList();
          }
          return const _WideChatHome();
        },
      ),
      bottomNavigationBar: context.isMobile ? const _MobileNavigation() : null,
    );
  }
}

class _WideChatHome extends StatelessWidget {
  const _WideChatHome();

  @override
  Widget build(BuildContext context) => Row(
        children: [
          Container(width: 64, color: LeoColors.rail, child: const _Rail()),
          SizedBox(
              width: context.isTablet ? 300 : 340,
              child: const _ConversationList()),
          const Expanded(child: _EmptyConversation()),
        ],
      );
}

class _Rail extends StatelessWidget {
  const _Rail();

  @override
  Widget build(BuildContext context) => const Column(
        children: [
          SizedBox(height: 24),
          Icon(Icons.forum_rounded, color: LeoColors.primary, size: 28),
          Spacer(),
          Icon(Icons.chat_bubble_outline, color: Colors.white, size: 22),
          SizedBox(height: 28),
          Icon(Icons.people_outline, color: LeoColors.textMuted, size: 22),
          SizedBox(height: 28),
          Icon(Icons.settings_outlined, color: LeoColors.textMuted, size: 22),
          SizedBox(height: 24),
        ],
      );
}

class _ConversationList extends StatelessWidget {
  const _ConversationList();

  @override
  Widget build(BuildContext context) => Container(
        color: LeoColors.surface,
        padding: const EdgeInsets.fromLTRB(20, 28, 20, 12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(children: [
              Text(AppStrings.messages,
                  style: TextStyle(fontSize: 24, fontWeight: FontWeight.w700)),
              Spacer(),
              Icon(Icons.add_circle_outline)
            ]),
            SizedBox(height: 24),
            TextField(
                decoration: InputDecoration(
                    hintText: AppStrings.searchMessages,
                    prefixIcon: Icon(Icons.search))),
            SizedBox(height: 20),
            Text('Your conversations',
                style: TextStyle(
                    fontWeight: FontWeight.w600,
                    color: LeoColors.textSecondary)),
            SizedBox(height: 20),
            Center(
                child: Text(AppStrings.noConversations,
                    style: TextStyle(color: LeoColors.textMuted))),
          ],
        ),
      );
}

class _EmptyConversation extends StatelessWidget {
  const _EmptyConversation();

  @override
  Widget build(BuildContext context) => const Center(
        child: Text(AppStrings.selectConversation,
            style: TextStyle(color: LeoColors.textMuted)),
      );
}

class _MobileNavigation extends StatelessWidget {
  const _MobileNavigation();

  @override
  Widget build(BuildContext context) => NavigationBar(
        selectedIndex: 0,
        destinations: [
          NavigationDestination(
              icon: Icon(Icons.chat_bubble_outline), label: AppStrings.chats),
          NavigationDestination(
              icon: Icon(Icons.people_outline), label: AppStrings.people),
          NavigationDestination(
              icon: Icon(Icons.settings_outlined), label: AppStrings.settings),
        ],
      );
}
