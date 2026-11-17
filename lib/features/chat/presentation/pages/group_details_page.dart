import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../../auth/presentation/providers/auth_notifier.dart';
import '../../../auth/presentation/states/auth_state.dart';
import '../../../users/domain/entities/user_search_item.dart';
import '../../../users/presentation/providers/users_providers.dart';
import '../../domain/entities/conversation.dart';
import '../../domain/entities/conversation_member.dart';
import 'shared_media_page.dart';

class GroupDetailsPage extends ConsumerStatefulWidget {
  const GroupDetailsPage({
    super.key,
    this.conversation,
    this.groupName = 'Group details',
    this.groupDescription = '',
  });

  final Conversation? conversation;
  final String groupName;
  final String groupDescription;

  @override
  ConsumerState<GroupDetailsPage> createState() => _GroupDetailsPageState();
}

class _GroupDetailsPageState extends ConsumerState<GroupDetailsPage> {
  bool _isLoadingMembers = false;
  List<ConversationMember> _members = [];
  bool _isMuted = false;

  @override
  void initState() {
    super.initState();
    _isMuted = widget.conversation?.isMuted ?? false;
    _loadMembers();
  }

  Future<void> _loadMembers() async {
    final convId = widget.conversation?.id;
    if (convId == null || convId.isEmpty) return;

    setState(() => _isLoadingMembers = true);
    final repo = ref.read(chatRepositoryProvider);
    final result = await repo.getMembers(convId);

    if (!mounted) return;
    setState(() => _isLoadingMembers = false);

    result.fold(
      (failure) {
        // Keep empty if load fails
      },
      (members) {
        setState(() => _members = members);
      },
    );
  }

  Future<void> _toggleMute() async {
    final convId = widget.conversation?.id;
    if (convId == null) return;

    final repo = ref.read(chatRepositoryProvider);
    if (_isMuted) {
      final res = await repo.unmuteConversation(convId);
      if (!mounted) return;
      res.fold(
        (f) => LeoSnackBar.showError(context, f.message),
        (_) {
          setState(() => _isMuted = false);
          LeoSnackBar.showSuccess(context, 'Notifications unmuted');
        },
      );
    } else {
      final res = await repo.muteConversation(
        conversationId: convId,
        mutedUntil:
            DateTime.now().add(const Duration(days: 365)).toIso8601String(),
      );
      if (!mounted) return;
      res.fold(
        (f) => LeoSnackBar.showError(context, f.message),
        (_) {
          setState(() => _isMuted = true);
          LeoSnackBar.showSuccess(context, 'Notifications muted');
        },
      );
    }
  }

  Future<void> _removeMember(ConversationMember member) async {
    final convId = widget.conversation?.id;
    if (convId == null) return;

    final repo = ref.read(chatRepositoryProvider);
    final res = await repo.removeMember(
      conversationId: convId,
      userId: member.userId,
    );

    res.fold(
      (failure) => LeoSnackBar.showError(context, failure.message),
      (_) {
        setState(() {
          _members.removeWhere((m) => m.userId == member.userId);
        });
        LeoSnackBar.showSuccess(
            context, 'Removed ${member.effectiveName} from group');
      },
    );
  }

  void _showAddMemberModal() {
    final convId = widget.conversation?.id;
    if (convId == null) return;

    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (ctx) => _AddMemberSheet(
        conversationId: convId,
        existingMemberIds: _members.map((m) => m.userId).toSet(),
        onMemberAdded: (user) {
          _loadMembers();
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authNotifierProvider);
    final currentUserId =
        (authState is Authenticated) ? authState.session.user.id : '';
    final title = widget.conversation?.title ?? widget.groupName;
    final description =
        widget.conversation?.lastMessageSnippet ?? widget.groupDescription;
    final isOwnerOrAdmin = _members.any(
      (m) =>
          m.userId == currentUserId &&
          (m.role.toUpperCase() == 'OWNER' || m.role.toUpperCase() == 'ADMIN'),
    );

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
          'Group details',
          style: TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.w900,
            letterSpacing: -0.5,
            color: LeoColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // Hero Sun Gradient Squircle / Avatar
              Container(
                width: 76,
                height: 76,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Color(0xFFFE9642), Color(0xFFDD3F3A)],
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                  ),
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: const [
                    BoxShadow(
                      color: Color(0x33E64A19),
                      blurRadius: 16,
                      offset: Offset(0, 6),
                    ),
                  ],
                ),
                child: const Icon(
                  Icons.groups_rounded,
                  color: Colors.white,
                  size: 38,
                ),
              ),
              const SizedBox(height: 14),

              Text(
                title,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.w900,
                  letterSpacing: -0.5,
                  color: LeoColors.textPrimary,
                ),
              ),
              if (description.isNotEmpty) ...[
                const SizedBox(height: 4),
                Text(
                  description,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: Color(0xFF78716C),
                  ),
                ),
              ],
              const SizedBox(height: 20),

              // Quick Action Buttons
              Row(
                children: [
                  Expanded(
                    child: _QuickActionButton(
                      icon: _isMuted
                          ? Icons.notifications_off_rounded
                          : Icons.notifications_off_outlined,
                      label: _isMuted ? 'Muted' : 'Mute',
                      onTap: _toggleMute,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _QuickActionButton(
                      icon: Icons.photo_library_outlined,
                      label: 'Media',
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute<void>(
                            builder: (_) => SharedMediaPage(
                              conversation: widget.conversation,
                              conversationTitle: 'Shared with $title',
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                  if (isOwnerOrAdmin) ...[
                    const SizedBox(width: 12),
                    Expanded(
                      child: _QuickActionButton(
                        icon: Icons.person_add_outlined,
                        label: 'Add',
                        onTap: _showAddMemberModal,
                      ),
                    ),
                  ],
                ],
              ),
              const SizedBox(height: 20),

              // Action List Cards
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: Column(
                  children: [
                    _DetailRow(
                      icon: Icons.photo_library_outlined,
                      title: 'Shared media & files',
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute<void>(
                            builder: (_) => SharedMediaPage(
                              conversation: widget.conversation,
                              conversationTitle: 'Shared with $title',
                            ),
                          ),
                        );
                      },
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),

              // Members Section Header
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Members · ${_isLoadingMembers ? "..." : _members.length}',
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                      color: LeoColors.textPrimary,
                    ),
                  ),
                  if (isOwnerOrAdmin)
                    LeoPressable(
                      onPressed: _showAddMemberModal,
                      child: const Row(
                        children: [
                          Icon(Icons.person_add_outlined,
                              color: LeoColors.primary, size: 18),
                          SizedBox(width: 4),
                          Text(
                            'Add',
                            style: TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w800,
                              color: LeoColors.primary,
                            ),
                          ),
                        ],
                      ),
                    ),
                ],
              ),
              const SizedBox(height: 14),

              // Members List Container
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: _isLoadingMembers
                    ? const Padding(
                        padding: EdgeInsets.all(24),
                        child: Center(
                          child: CircularProgressIndicator(
                              color: LeoColors.primary),
                        ),
                      )
                    : _members.isEmpty
                        ? const Padding(
                            padding: EdgeInsets.all(24),
                            child: Center(
                              child: Text(
                                'No members found',
                                style: TextStyle(
                                  color: LeoColors.textSecondary,
                                  fontSize: 14,
                                ),
                              ),
                            ),
                          )
                        : ListView.separated(
                            shrinkWrap: true,
                            physics: const NeverScrollableScrollPhysics(),
                            itemCount: _members.length,
                            separatorBuilder: (_, __) => const Divider(
                                height: 1,
                                indent: 68,
                                color: Color(0xFFF3ECE5)),
                            itemBuilder: (context, index) {
                              final member = _members[index];
                              final isSelf = member.userId == currentUserId;
                              final roleUpper = member.role.toUpperCase();

                              return Padding(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 16, vertical: 12),
                                child: Row(
                                  children: [
                                    LeoAvatar(
                                      radius: 22,
                                      name: member.effectiveName,
                                    ),
                                    const SizedBox(width: 14),
                                    Expanded(
                                      child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                            isSelf
                                                ? '${member.effectiveName} (You)'
                                                : member.effectiveName,
                                            style: const TextStyle(
                                              fontSize: 15,
                                              fontWeight: FontWeight.w800,
                                              color: LeoColors.textPrimary,
                                            ),
                                          ),
                                          if (member.username != null)
                                            Text(
                                              '@${member.username}',
                                              style: const TextStyle(
                                                fontSize: 12,
                                                color: LeoColors.textSecondary,
                                              ),
                                            ),
                                        ],
                                      ),
                                    ),
                                    if (roleUpper == 'OWNER' ||
                                        roleUpper == 'ADMIN') ...[
                                      Container(
                                        padding: const EdgeInsets.symmetric(
                                            horizontal: 10, vertical: 3),
                                        decoration: BoxDecoration(
                                          color: roleUpper == 'OWNER'
                                              ? const Color(0xFFFFEEE3)
                                              : const Color(0xFFF1F5F9),
                                          borderRadius:
                                              BorderRadius.circular(10),
                                        ),
                                        child: Text(
                                          roleUpper == 'OWNER'
                                              ? 'Owner'
                                              : 'Admin',
                                          style: TextStyle(
                                            fontSize: 11,
                                            fontWeight: FontWeight.w800,
                                            color: roleUpper == 'OWNER'
                                                ? const Color(0xFFC05621)
                                                : const Color(0xFF475569),
                                          ),
                                        ),
                                      ),
                                      const SizedBox(width: 8),
                                    ],
                                    if (isOwnerOrAdmin &&
                                        !isSelf &&
                                        roleUpper != 'OWNER')
                                      LeoPressable(
                                        onPressed: () => _removeMember(member),
                                        child: const Padding(
                                          padding: EdgeInsets.all(4),
                                          child: Text(
                                            'Remove',
                                            style: TextStyle(
                                              fontSize: 13,
                                              fontWeight: FontWeight.w800,
                                              color: Color(0xFFDC2626),
                                            ),
                                          ),
                                        ),
                                      ),
                                  ],
                                ),
                              );
                            },
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

class _AddMemberSheet extends ConsumerStatefulWidget {
  const _AddMemberSheet({
    required this.conversationId,
    required this.existingMemberIds,
    required this.onMemberAdded,
  });

  final String conversationId;
  final Set<String> existingMemberIds;
  final ValueChanged<UserSearchItem> onMemberAdded;

  @override
  ConsumerState<_AddMemberSheet> createState() => _AddMemberSheetState();
}

class _AddMemberSheetState extends ConsumerState<_AddMemberSheet> {
  final _searchController = TextEditingController();
  bool _isAdding = false;

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _addMember(UserSearchItem user) async {
    if (_isAdding) return;
    setState(() => _isAdding = true);

    final repo = ref.read(chatRepositoryProvider);
    final res = await repo.addGroupMembers(
      conversationId: widget.conversationId,
      memberIds: [user.id],
    );

    if (!mounted) return;
    setState(() => _isAdding = false);

    res.fold(
      (failure) => LeoSnackBar.showError(context, failure.message),
      (_) {
        widget.onMemberAdded(user);
        Navigator.of(context).pop();
        LeoSnackBar.showSuccess(
            context, 'Added ${user.effectiveName} to group');
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final searchResults = ref.watch(userSearchResultsProvider);

    return SafeArea(
      child: Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
          left: 20,
          right: 20,
          top: 16,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: const Color(0xFFCBD5E1),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 16),
            const Text(
              'Add group member',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: LeoColors.textPrimary,
              ),
            ),
            const SizedBox(height: 12),
            Container(
              height: 46,
              padding: const EdgeInsets.symmetric(horizontal: 14),
              decoration: BoxDecoration(
                color: const Color(0xFFF3ECE5),
                borderRadius: BorderRadius.circular(23),
              ),
              child: Row(
                children: [
                  const Icon(Icons.search_rounded,
                      size: 20, color: Color(0xFF78716C)),
                  const SizedBox(width: 8),
                  Expanded(
                    child: TextField(
                      controller: _searchController,
                      onChanged: (val) {
                        ref.read(userSearchQueryProvider.notifier).state =
                            val.trim();
                      },
                      decoration: const InputDecoration(
                        hintText: 'Search people to add...',
                        border: InputBorder.none,
                        isDense: true,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 260,
              child: searchResults.when(
                loading: () => const Center(
                  child: CircularProgressIndicator(color: LeoColors.primary),
                ),
                error: (e, _) => Center(child: Text('Search error: $e')),
                data: (users) {
                  final eligibleUsers = users
                      .where((u) => !widget.existingMemberIds.contains(u.id))
                      .toList();

                  if (eligibleUsers.isEmpty) {
                    return const Center(
                      child: Text(
                        'No matching users to add',
                        style: TextStyle(
                          color: LeoColors.textSecondary,
                          fontSize: 14,
                        ),
                      ),
                    );
                  }

                  return ListView.separated(
                    itemCount: eligibleUsers.length,
                    separatorBuilder: (_, __) => const Divider(
                        height: 1, indent: 56, color: Color(0xFFF3ECE5)),
                    itemBuilder: (ctx, index) {
                      final u = eligibleUsers[index];
                      return LeoPressable(
                        onPressed: () => _addMember(u),
                        child: Padding(
                          padding: const EdgeInsets.symmetric(vertical: 10),
                          child: Row(
                            children: [
                              LeoAvatar(
                                radius: 20,
                                name: u.effectiveName,
                                imageUrl: u.profilePictureUrl,
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Text(
                                  u.effectiveName,
                                  style: const TextStyle(
                                    fontSize: 15,
                                    fontWeight: FontWeight.w700,
                                    color: LeoColors.textPrimary,
                                  ),
                                ),
                              ),
                              const Icon(Icons.add_circle_outline_rounded,
                                  color: LeoColors.primary, size: 22),
                            ],
                          ),
                        ),
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _QuickActionButton extends StatelessWidget {
  const _QuickActionButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return LeoPressable(
      onPressed: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: const Color(0xFFE7DFD8)),
          boxShadow: const [
            BoxShadow(
              color: Color(0x06000000),
              blurRadius: 6,
              offset: Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          children: [
            Icon(icon, color: LeoColors.primary, size: 22),
            const SizedBox(height: 6),
            Text(
              label,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: LeoColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  const _DetailRow({
    required this.icon,
    required this.title,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return LeoPressable(
      onPressed: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        child: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: const Color(0xFFF3ECE5),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(icon, color: LeoColors.textPrimary, size: 20),
            ),
            const SizedBox(width: 14),
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
            const Icon(
              Icons.chevron_right_rounded,
              color: Color(0xFF78716C),
              size: 22,
            ),
          ],
        ),
      ),
    );
  }
}
