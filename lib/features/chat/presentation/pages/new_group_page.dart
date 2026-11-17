import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_primary_button.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../users/domain/entities/user_search_item.dart';
import '../../../users/presentation/providers/users_providers.dart';
import '../providers/chat_notifier.dart';

class NewGroupPage extends ConsumerStatefulWidget {
  const NewGroupPage({super.key});

  @override
  ConsumerState<NewGroupPage> createState() => _NewGroupPageState();
}

class _NewGroupPageState extends ConsumerState<NewGroupPage> {
  final _titleController = TextEditingController();
  final _searchController = TextEditingController();
  final Set<UserSearchItem> _selectedMembers = {};
  bool _isCreating = false;

  @override
  void dispose() {
    _titleController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  void _toggleUser(UserSearchItem user) {
    setState(() {
      if (_selectedMembers.any((u) => u.id == user.id)) {
        _selectedMembers.removeWhere((u) => u.id == user.id);
      } else {
        _selectedMembers.add(user);
      }
    });
  }

  Future<void> _handleCreateGroup() async {
    final title = _titleController.text.trim();
    if (title.isEmpty || _selectedMembers.isEmpty) return;

    setState(() => _isCreating = true);
    final result = await ref.read(chatNotifierProvider.notifier).createGroup(
          title: title,
          memberIds: _selectedMembers.map((u) => u.id).toList(),
        );

    if (!mounted) return;
    setState(() => _isCreating = false);

    result.fold(
      (failure) {
        LeoSnackBar.showError(context, failure.message);
      },
      (conversation) {
        LeoSnackBar.showSuccess(context, 'Group "$title" created!');
        context.pop();
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final searchResults = ref.watch(userSearchResultsProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: const Color(0xFFFAF7F4),
        elevation: 0,
        leading: LeoPressable(
          child: IconButton(
            icon: const Icon(Icons.arrow_back_rounded,
                color: LeoColors.textPrimary),
            onPressed: () => context.pop(),
          ),
        ),
        title: const Text(
          'New Group',
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: LeoColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Group Title Input Card
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: LeoColors.border),
                ),
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'GROUP DETAILS',
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.w800,
                        letterSpacing: 0.6,
                        color: LeoColors.textSecondary,
                      ),
                    ),
                    const SizedBox(height: 10),
                    TextField(
                      controller: _titleController,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                        color: LeoColors.textPrimary,
                      ),
                      decoration: const InputDecoration(
                        hintText: 'e.g. Design Team or Weekend Trip',
                        hintStyle: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w400,
                          color: LeoColors.textMuted,
                        ),
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                        filled: false,
                        fillColor: Colors.transparent,
                        prefixIcon: Icon(Icons.groups_rounded,
                            color: LeoColors.primary),
                      ),
                      onChanged: (_) => setState(() {}),
                    ),
                  ],
                ),
              ),
            ),

            // Selected Members Bar (if any)
            if (_selectedMembers.isNotEmpty)
              Container(
                height: 52,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  itemCount: _selectedMembers.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 8),
                  itemBuilder: (context, index) {
                    final member = _selectedMembers.elementAt(index);
                    return Chip(
                      backgroundColor: LeoColors.primarySoft,
                      side: BorderSide.none,
                      avatar: LeoAvatar(
                        name: member.effectiveName,
                        imageUrl: member.profilePictureUrl,
                        radius: 12,
                      ),
                      label: Text(
                        member.effectiveName,
                        style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                          color: LeoColors.primary,
                        ),
                      ),
                      deleteIcon: const Icon(
                        Icons.close_rounded,
                        size: 14,
                        color: LeoColors.primary,
                      ),
                      onDeleted: () => _toggleUser(member),
                    );
                  },
                ),
              ),

            // Search input
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: LeoColors.border),
                ),
                child: TextField(
                  controller: _searchController,
                  onChanged: (val) {
                    ref.read(userSearchQueryProvider.notifier).state = val;
                  },
                  decoration: const InputDecoration(
                    hintText: 'Search people to add...',
                    hintStyle: TextStyle(
                      fontSize: 14,
                      color: LeoColors.textMuted,
                    ),
                    prefixIcon: Icon(Icons.search_rounded,
                        color: LeoColors.textSecondary),
                    border: InputBorder.none,
                    enabledBorder: InputBorder.none,
                    focusedBorder: InputBorder.none,
                    filled: false,
                    fillColor: Colors.transparent,
                    contentPadding: EdgeInsets.symmetric(vertical: 14),
                  ),
                ),
              ),
            ),

            // Search Results List
            Expanded(
              child: searchResults.when(
                data: (users) {
                  if (_searchController.text.trim().isEmpty) {
                    return Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            Icons.person_search_rounded,
                            size: 48,
                            color: LeoColors.textMuted.withOpacity(0.6),
                          ),
                          const SizedBox(height: 12),
                          const Text(
                            'Search users by username or display name',
                            style: TextStyle(
                              fontSize: 14,
                              color: LeoColors.textSecondary,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                    );
                  }

                  if (users.isEmpty) {
                    return Center(
                      child: Text(
                        'No users found matching "${_searchController.text}"',
                        style: const TextStyle(
                          fontSize: 14,
                          color: LeoColors.textSecondary,
                        ),
                      ),
                    );
                  }

                  return ListView.separated(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                    itemCount: users.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 6),
                    itemBuilder: (context, index) {
                      final user = users[index];
                      final isSelected =
                          _selectedMembers.any((u) => u.id == user.id);

                      return LeoPressable(
                        onPressed: () => _toggleUser(user),
                        child: Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: isSelected
                                ? LeoColors.primarySoft.withOpacity(0.5)
                                : Colors.white,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(
                              color: isSelected
                                  ? LeoColors.primary
                                  : LeoColors.border,
                            ),
                          ),
                          child: Row(
                            children: [
                              LeoAvatar(
                                name: user.effectiveName,
                                imageUrl: user.profilePictureUrl,
                                radius: 22,
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
                              Icon(
                                isSelected
                                    ? Icons.check_circle_rounded
                                    : Icons.radio_button_unchecked_rounded,
                                color: isSelected
                                    ? LeoColors.primary
                                    : LeoColors.textMuted,
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  );
                },
                loading: () => const Center(
                  child: CircularProgressIndicator(color: LeoColors.primary),
                ),
                error: (e, _) => Center(
                  child: Text('Search failed: $e'),
                ),
              ),
            ),

            // Bottom Action Bar
            Padding(
              padding: const EdgeInsets.all(16),
              child: SizedBox(
                width: double.infinity,
                height: 52,
                child: LeoPrimaryButton(
                  label: _selectedMembers.isEmpty
                      ? 'Select at least 1 member'
                      : 'Create Group (${_selectedMembers.length})',
                  icon: Icons.check_rounded,
                  isLoading: _isCreating,
                  onPressed: (_titleController.text.trim().isEmpty ||
                          _selectedMembers.isEmpty)
                      ? null
                      : _handleCreateGroup,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
