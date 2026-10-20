import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../domain/entities/conversation.dart';
import '../providers/chat_notifier.dart';
import 'group_details_page.dart';

class NewConversationPage extends ConsumerStatefulWidget {
  const NewConversationPage({super.key});

  @override
  ConsumerState<NewConversationPage> createState() =>
      _NewConversationPageState();
}

class _NewConversationPageState extends ConsumerState<NewConversationPage> {
  final _searchController = TextEditingController();
  String _searchQuery = '';

  final _contacts = const [
    _ContactItem(
      id: 'c-ellie',
      name: 'Ellie Park',
      username: 'elliepark',
      avatarUrl: 'assets/avatars/ellie.png',
      isOnline: false,
    ),
    _ContactItem(
      id: 'c-maya',
      name: 'Maya Chen',
      username: 'mayachen',
      avatarUrl: 'assets/avatars/maya.png',
      isOnline: true,
    ),
    _ContactItem(
      id: 'c-noah',
      name: 'Noah Williams',
      username: 'noahw',
      avatarUrl: 'assets/avatars/noah.png',
      isOnline: true,
    ),
    _ContactItem(
      id: 'c-sofia',
      name: 'Sofia Rivera',
      username: 'sofiar',
      avatarUrl: 'assets/avatars/sofia.png',
      isOnline: false,
    ),
  ];

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _openChatWithContact(_ContactItem contact) {
    final conversation = Conversation(
      id: contact.id,
      title: contact.name,
      lastMessageSnippet: 'Hey there! How is it going?',
      lastMessageAt: DateTime.now(),
      unreadCount: 0,
      avatarUrl: contact.avatarUrl,
      isOnline: contact.isOnline,
      type: 'DIRECT',
    );

    ref.read(chatNotifierProvider.notifier).selectConversation(conversation);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final filteredContacts = _contacts.where((c) {
      if (_searchQuery.isEmpty) return true;
      return c.name.toLowerCase().contains(_searchQuery) ||
          c.username.toLowerCase().contains(_searchQuery);
    }).toList();

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: const Color(0xFFFAF7F4),
        elevation: 0,
        scrolledUnderElevation: 0,
        leading: LeoPressable(
          child: IconButton(
            icon: const Icon(Icons.arrow_back_rounded,
                color: LeoColors.textPrimary),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ),
        title: const Text(
          'New conversation',
          style: TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.w900,
            letterSpacing: -0.5,
            color: LeoColors.textPrimary,
          ),
        ),
        actions: [
          LeoPressable(
            child: IconButton(
              icon: const Icon(Icons.close_rounded,
                  color: LeoColors.textPrimary, size: 24),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Search Bar
              Container(
                height: 48,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                decoration: BoxDecoration(
                  color: const Color(0xFFF3ECE5),
                  borderRadius: BorderRadius.circular(24),
                ),
                child: Row(
                  children: [
                    const Icon(
                      Icons.search_rounded,
                      color: Color(0xFF78716C),
                      size: 22,
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: TextField(
                        controller: _searchController,
                        onChanged: (val) => setState(
                            () => _searchQuery = val.trim().toLowerCase()),
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w500,
                          color: LeoColors.textPrimary,
                        ),
                        decoration: const InputDecoration(
                          hintText: 'Search by name or username',
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
                    if (_searchController.text.isNotEmpty)
                      GestureDetector(
                        onTap: () {
                          _searchController.clear();
                          setState(() => _searchQuery = '');
                        },
                        child: const Icon(
                          Icons.close_rounded,
                          color: Color(0xFF78716C),
                          size: 18,
                        ),
                      ),
                  ],
                ),
              ),
              const SizedBox(height: 18),

              // Action Cards: Create a group & Invite a friend
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: Column(
                  children: [
                    _ActionTile(
                      icon: Icons.groups_rounded,
                      title: 'Create a group',
                      subtitle: 'Bring your people together',
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute<void>(
                            builder: (_) => const GroupDetailsPage(),
                          ),
                        );
                      },
                    ),
                    const Divider(
                        height: 1, indent: 64, color: Color(0xFFF3ECE5)),
                    _ActionTile(
                      icon: Icons.person_add_rounded,
                      title: 'Invite a friend',
                      subtitle: 'A good chat starts with a hello',
                      onTap: () {},
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),

              // YOUR FAVORITES
              const Text(
                'YOUR FAVORITES',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 0.6,
                  color: Color(0xFF78716C),
                ),
              ),
              const SizedBox(height: 14),

              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: _contacts.map((contact) {
                  return LeoPressable(
                    onPressed: () => _openChatWithContact(contact),
                    child: Column(
                      children: [
                        LeoAvatar(
                          radius: 28,
                          imageUrl: contact.avatarUrl,
                          assetPath: contact.avatarUrl,
                          hasOnlineBadge: contact.isOnline,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          contact.name.split(' ').first,
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w800,
                            color: LeoColors.textPrimary,
                          ),
                        ),
                      ],
                    ),
                  );
                }).toList(),
              ),
              const SizedBox(height: 26),

              // ON LEOCHAT
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'ON LEOCHAT',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                      letterSpacing: 0.6,
                      color: Color(0xFF78716C),
                    ),
                  ),
                  Text(
                    '${filteredContacts.length} contacts',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: Color(0xFF78716C),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              // Contact Rows
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: ListView.separated(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: filteredContacts.length,
                  separatorBuilder: (_, __) => const Divider(
                      height: 1, indent: 68, color: Color(0xFFF3ECE5)),
                  itemBuilder: (context, index) {
                    final contact = filteredContacts[index];
                    return InkWell(
                      onTap: () => _openChatWithContact(contact),
                      borderRadius: BorderRadius.circular(22),
                      child: Padding(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 16, vertical: 12),
                        child: Row(
                          children: [
                            LeoAvatar(
                              radius: 22,
                              imageUrl: contact.avatarUrl,
                              assetPath: contact.avatarUrl,
                            ),
                            const SizedBox(width: 14),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    contact.name,
                                    style: const TextStyle(
                                      fontSize: 15,
                                      fontWeight: FontWeight.w800,
                                      color: LeoColors.textPrimary,
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    '@${contact.username}',
                                    style: const TextStyle(
                                      fontSize: 13,
                                      fontWeight: FontWeight.w500,
                                      color: Color(0xFF78716C),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            LeoPressable(
                              onPressed: () => _openChatWithContact(contact),
                              child: Container(
                                width: 36,
                                height: 36,
                                decoration: BoxDecoration(
                                  color: Colors.transparent,
                                  shape: BoxShape.circle,
                                  border: Border.all(
                                      color: LeoColors.primary, width: 1.5),
                                ),
                                child: const Icon(
                                  Icons.chat_bubble_outline_rounded,
                                  color: LeoColors.primary,
                                  size: 18,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 22),

              // Bottom Callout Card
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(18),
                decoration: BoxDecoration(
                  color: const Color(0xFFFFEEE3),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: const Color(0xFFFFD7C2)),
                ),
                child: const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "Can't find someone?",
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w800,
                        color: LeoColors.textPrimary,
                      ),
                    ),
                    SizedBox(height: 5),
                    Text(
                      'Try their exact username, or send them an invite to join your conversations.',
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        height: 1.4,
                        color: Color(0xFF78716C),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}

class _ContactItem {
  const _ContactItem({
    required this.id,
    required this.name,
    required this.username,
    required this.avatarUrl,
    required this.isOnline,
  });

  final String id;
  final String name;
  final String username;
  final String avatarUrl;
  final bool isOnline;
}

class _ActionTile extends StatelessWidget {
  const _ActionTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(22),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: const Color(0xFFFFEEE3),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: LeoColors.primary, size: 24),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w800,
                      color: LeoColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    subtitle,
                    style: const TextStyle(
                      fontSize: 12.5,
                      fontWeight: FontWeight.w500,
                      color: Color(0xFF78716C),
                    ),
                  ),
                ],
              ),
            ),
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
