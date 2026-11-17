import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../users/domain/entities/user_search_item.dart';
import '../../../users/presentation/providers/users_providers.dart';
import '../../domain/entities/conversation.dart';
import '../providers/chat_notifier.dart';
import 'new_group_page.dart';

class NewConversationPage extends ConsumerStatefulWidget {
  const NewConversationPage({super.key});

  @override
  ConsumerState<NewConversationPage> createState() =>
      _NewConversationPageState();
}

class _NewConversationPageState extends ConsumerState<NewConversationPage> {
  final _searchController = TextEditingController();
  bool _isStartingChat = false;

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _startChatWithUser(UserSearchItem user) async {
    if (_isStartingChat) return;
    setState(() => _isStartingChat = true);

    final result = await ref
        .read(chatNotifierProvider.notifier)
        .startDirectMessage(user.id);

    if (!mounted) return;
    setState(() => _isStartingChat = false);

    result.fold(
      (failure) {
        LeoSnackBar.showError(context, failure.message);
      },
      (conversation) {
        Navigator.of(context).pop();
      },
    );
  }

  void _openExistingConversation(Conversation conversation) {
    ref.read(chatNotifierProvider.notifier).selectConversation(conversation);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final searchQuery = ref.watch(userSearchQueryProvider);
    final searchResults = ref.watch(userSearchResultsProvider);
    final chatState = ref.watch(chatNotifierProvider);

    final recentDirectChats =
        chatState.conversations.where((c) => !c.isGroup && !c.isAi).toList();

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
        child: Column(
          children: [
            // Search Input Header
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
              child: Container(
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
                        onChanged: (val) {
                          ref.read(userSearchQueryProvider.notifier).state =
                              val.trim();
                        },
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
                          ref.read(userSearchQueryProvider.notifier).state = '';
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
            ),

            if (_isStartingChat)
              const LinearProgressIndicator(
                backgroundColor: Colors.transparent,
                color: LeoColors.primary,
                minHeight: 2,
              ),

            // Content Area
            Expanded(
              child: searchQuery.isNotEmpty
                  ? _buildSearchResults(searchResults)
                  : _buildDefaultContent(recentDirectChats),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSearchResults(AsyncValue<List<UserSearchItem>> searchResults) {
    return searchResults.when(
      loading: () => const Center(
        child: Padding(
          padding: EdgeInsets.all(32),
          child: CircularProgressIndicator(color: LeoColors.primary),
        ),
      ),
      error: (e, _) => Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline_rounded,
                  size: 40, color: Color(0xFFEF4444)),
              const SizedBox(height: 12),
              Text(
                'Could not load search results: $e',
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: LeoColors.textSecondary,
                  fontSize: 14,
                ),
              ),
            ],
          ),
        ),
      ),
      data: (users) {
        if (users.isEmpty) {
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(32),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    width: 56,
                    height: 56,
                    decoration: BoxDecoration(
                      color: const Color(0xFFF3ECE5),
                      borderRadius: BorderRadius.circular(28),
                    ),
                    child: const Icon(
                      Icons.person_search_rounded,
                      size: 28,
                      color: Color(0xFF78716C),
                    ),
                  ),
                  const SizedBox(height: 16),
                  const Text(
                    'No people found',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: LeoColors.textPrimary,
                    ),
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Check the spelling or try searching for another username.',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: 13,
                      color: LeoColors.textSecondary,
                    ),
                  ),
                ],
              ),
            ),
          );
        }

        return ListView.separated(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          itemCount: users.length,
          separatorBuilder: (_, __) =>
              const Divider(height: 1, indent: 64, color: Color(0xFFF3ECE5)),
          itemBuilder: (context, index) {
            final user = users[index];
            return LeoPressable(
              onPressed: () => _startChatWithUser(user),
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 12),
                color: Colors.transparent,
                child: Row(
                  children: [
                    LeoAvatar(
                      radius: 24,
                      name: user.effectiveName,
                      imageUrl: user.profilePictureUrl,
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            user.effectiveName,
                            style: const TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w700,
                              color: LeoColors.textPrimary,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            '@${user.username}',
                            style: const TextStyle(
                              fontSize: 13,
                              color: LeoColors.textSecondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const Icon(
                      Icons.chat_bubble_outline_rounded,
                      size: 20,
                      color: LeoColors.primary,
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildDefaultContent(List<Conversation> recentChats) {
    return ListView(
      physics: const BouncingScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
      children: [
        // Action Cards: Create a group
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: const Color(0xFFE7DFD8)),
            boxShadow: const [
              BoxShadow(
                color: Color(0x06000000),
                blurRadius: 8,
                offset: Offset(0, 3),
              ),
            ],
          ),
          child: LeoPressable(
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute<void>(
                  builder: (_) => const NewGroupPage(),
                ),
              );
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
              child: Row(
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: LeoColors.primarySoft,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(
                      Icons.groups_rounded,
                      color: LeoColors.primary,
                      size: 24,
                    ),
                  ),
                  const SizedBox(width: 14),
                  const Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Create a group',
                          style: TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w800,
                            color: LeoColors.textPrimary,
                          ),
                        ),
                        SizedBox(height: 2),
                        Text(
                          'Bring your team or friends together',
                          style: TextStyle(
                            fontSize: 13,
                            color: LeoColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const Icon(
                    Icons.chevron_right_rounded,
                    color: Color(0xFF78716C),
                    size: 22,
                  ),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(height: 24),

        // RECENT CONTACTS
        if (recentChats.isNotEmpty) ...[
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'RECENT CONTACTS',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 0.6,
                  color: Color(0xFF78716C),
                ),
              ),
              Text(
                '${recentChats.length} people',
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: Color(0xFF78716C),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(22),
              border: Border.all(color: const Color(0xFFE7DFD8)),
            ),
            child: ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: recentChats.length,
              separatorBuilder: (_, __) => const Divider(
                  height: 1, indent: 64, color: Color(0xFFF3ECE5)),
              itemBuilder: (context, index) {
                final chat = recentChats[index];
                return LeoPressable(
                  onPressed: () => _openExistingConversation(chat),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 16, vertical: 12),
                    child: Row(
                      children: [
                        LeoAvatar(
                          radius: 22,
                          name: chat.title,
                          imageUrl: chat.preferredAvatarUrl,
                          hasOnlineBadge: chat.isOnline,
                        ),
                        const SizedBox(width: 14),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                chat.title,
                                style: const TextStyle(
                                  fontSize: 15,
                                  fontWeight: FontWeight.w700,
                                  color: LeoColors.textPrimary,
                                ),
                              ),
                              if (chat.lastMessageSnippet != null &&
                                  chat.lastMessageSnippet!.isNotEmpty) ...[
                                const SizedBox(height: 2),
                                Text(
                                  chat.lastMessageSnippet!,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                    fontSize: 13,
                                    color: LeoColors.textSecondary,
                                  ),
                                ),
                              ],
                            ],
                          ),
                        ),
                        const Icon(
                          Icons.chevron_right_rounded,
                          color: Color(0xFFCBD5E1),
                          size: 20,
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
        ] else ...[
          // Empty State when no recent contacts and no search query
          Container(
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(22),
              border: Border.all(color: const Color(0xFFE7DFD8)),
            ),
            child: Column(
              children: [
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: LeoColors.primarySoft,
                    borderRadius: BorderRadius.circular(28),
                  ),
                  child: const Icon(
                    Icons.people_outline_rounded,
                    size: 28,
                    color: LeoColors.primary,
                  ),
                ),
                const SizedBox(height: 16),
                const Text(
                  'Start a new conversation',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w800,
                    color: LeoColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 6),
                const Text(
                  'Search for people above by their username or display name to begin chatting.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 13,
                    color: LeoColors.textSecondary,
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }
}
