import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../domain/entities/admin_user.dart';
import '../providers/admin_providers.dart';

class AdminUsersPage extends ConsumerWidget {
  const AdminUsersPage({super.key});

  static const _filters = ['ALL', 'ACTIVE', 'DISABLED', 'BANNED'];

  void _showUserActions(BuildContext context, WidgetRef ref, AdminUser user) {
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (bottomSheetContext) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
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
                Row(
                  children: [
                    LeoAvatar(
                      name: user.effectiveName,
                      imageUrl: user.profilePictureUrl,
                      radius: 20,
                    ),
                    const SizedBox(width: 12),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          user.effectiveName,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w800,
                            color: LeoColors.textPrimary,
                          ),
                        ),
                        Text(
                          '@${user.username} · ${user.status}',
                          style: const TextStyle(
                            fontSize: 13,
                            color: LeoColors.textSecondary,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                const SizedBox(height: 20),

                // Set Active
                if (!user.isActive)
                  ListTile(
                    leading: const Icon(Icons.check_circle_outline,
                        color: Color(0xFF10B981)),
                    title: const Text('Mark as ACTIVE'),
                    onTap: () async {
                      Navigator.pop(bottomSheetContext);
                      final res = await ref
                          .read(adminRepositoryProvider)
                          .updateUserStatus(
                            userId: user.id,
                            status: 'ACTIVE',
                          );
                      res.fold(
                        (f) => LeoSnackBar.showError(context, f.message),
                        (_) {
                          LeoSnackBar.showSuccess(
                              context, 'User marked ACTIVE');
                          ref.invalidate(adminUsersListProvider);
                        },
                      );
                    },
                  ),

                // Set Disabled
                if (!user.isDisabled)
                  ListTile(
                    leading: const Icon(Icons.pause_circle_outline,
                        color: Colors.amber),
                    title: const Text('Mark as DISABLED'),
                    onTap: () async {
                      Navigator.pop(bottomSheetContext);
                      final res = await ref
                          .read(adminRepositoryProvider)
                          .updateUserStatus(
                            userId: user.id,
                            status: 'DISABLED',
                          );
                      res.fold(
                        (f) => LeoSnackBar.showError(context, f.message),
                        (_) {
                          LeoSnackBar.showSuccess(
                              context, 'User status changed to DISABLED');
                          ref.invalidate(adminUsersListProvider);
                        },
                      );
                    },
                  ),

                // Set Banned
                if (!user.isBanned)
                  ListTile(
                    leading: const Icon(Icons.block_rounded,
                        color: Color(0xFFEF4444)),
                    title: const Text('Mark as BANNED'),
                    onTap: () async {
                      Navigator.pop(bottomSheetContext);
                      final res = await ref
                          .read(adminRepositoryProvider)
                          .updateUserStatus(
                            userId: user.id,
                            status: 'BANNED',
                          );
                      res.fold(
                        (f) => LeoSnackBar.showError(context, f.message),
                        (_) {
                          LeoSnackBar.showSuccess(
                              context, 'User status changed to BANNED');
                          ref.invalidate(adminUsersListProvider);
                        },
                      );
                    },
                  ),

                // Force Logout
                ListTile(
                  leading: const Icon(Icons.logout_rounded,
                      color: Color(0xFF64748B)),
                  title: const Text('Force Logout from all devices'),
                  onTap: () async {
                    Navigator.pop(bottomSheetContext);
                    final res = await ref
                        .read(adminRepositoryProvider)
                        .forceLogoutUser(user.id);
                    res.fold(
                      (f) => LeoSnackBar.showError(context, f.message),
                      (_) => LeoSnackBar.showSuccess(
                          context, 'Force logged out sessions'),
                    );
                  },
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentFilter = ref.watch(adminUsersFilterProvider);
    final usersAsync = ref.watch(adminUsersListProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        scrolledUnderElevation: 1,
        leading: LeoPressable(
          child: IconButton(
            icon: const Icon(Icons.arrow_back_rounded,
                color: LeoColors.textPrimary),
            onPressed: () => context.pop(),
          ),
        ),
        title: const Text(
          AppStrings.userManagement,
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w800,
            color: LeoColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Search Input
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: LeoColors.border),
                ),
                child: TextField(
                  onChanged: (val) {
                    ref.read(adminUsersQueryProvider.notifier).state = val;
                  },
                  decoration: const InputDecoration(
                    hintText: 'Search by username or display name...',
                    hintStyle: TextStyle(
                      fontSize: 14,
                      color: LeoColors.textMuted,
                    ),
                    prefixIcon: Icon(Icons.search_rounded,
                        color: LeoColors.textSecondary),
                    border: InputBorder.none,
                    contentPadding: EdgeInsets.symmetric(vertical: 14),
                  ),
                ),
              ),
            ),

            // Filter Tabs
            Container(
              height: 40,
              margin: const EdgeInsets.symmetric(vertical: 4),
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                itemCount: _filters.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, index) {
                  final filter = _filters[index];
                  final isSelected = filter == currentFilter;

                  return LeoPressable(
                    onPressed: () {
                      ref.read(adminUsersFilterProvider.notifier).state =
                          filter;
                    },
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 14, vertical: 8),
                      decoration: BoxDecoration(
                        color: isSelected ? LeoColors.primary : Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        border: Border.all(
                          color:
                              isSelected ? LeoColors.primary : LeoColors.border,
                        ),
                      ),
                      child: Text(
                        filter,
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                          color: isSelected
                              ? Colors.white
                              : LeoColors.textSecondary,
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),

            // User List
            Expanded(
              child: usersAsync.when(
                data: (users) {
                  if (users.isEmpty) {
                    return const Center(
                      child: Text(
                        'No users found matching query',
                        style: TextStyle(
                          fontSize: 14,
                          color: LeoColors.textSecondary,
                        ),
                      ),
                    );
                  }

                  return ListView.separated(
                    padding: const EdgeInsets.all(16),
                    itemCount: users.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (context, index) {
                      final user = users[index];

                      Color badgeColor = const Color(0xFF10B981);
                      if (user.isDisabled) badgeColor = Colors.amber;
                      if (user.isBanned) badgeColor = const Color(0xFFEF4444);

                      return LeoPressable(
                        onPressed: () => _showUserActions(context, ref, user),
                        child: Container(
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(18),
                            border: Border.all(color: LeoColors.border),
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
                                    Row(
                                      children: [
                                        Text(
                                          user.effectiveName,
                                          style: const TextStyle(
                                            fontSize: 15,
                                            fontWeight: FontWeight.w700,
                                            color: LeoColors.textPrimary,
                                          ),
                                        ),
                                        if (user.isAdmin) ...[
                                          const SizedBox(width: 6),
                                          Container(
                                            padding: const EdgeInsets.symmetric(
                                                horizontal: 6, vertical: 1.5),
                                            decoration: BoxDecoration(
                                              color: LeoColors.primary,
                                              borderRadius:
                                                  BorderRadius.circular(4),
                                            ),
                                            child: const Text(
                                              'ADMIN',
                                              style: TextStyle(
                                                fontSize: 9,
                                                fontWeight: FontWeight.w900,
                                                color: Colors.white,
                                              ),
                                            ),
                                          ),
                                        ],
                                      ],
                                    ),
                                    const SizedBox(height: 2),
                                    Text(
                                      '@${user.username} · ${user.email}',
                                      style: const TextStyle(
                                        fontSize: 12,
                                        color: LeoColors.textSecondary,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 8, vertical: 3),
                                decoration: BoxDecoration(
                                  color: badgeColor.withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(8),
                                ),
                                child: Text(
                                  user.status,
                                  style: TextStyle(
                                    fontSize: 10,
                                    fontWeight: FontWeight.w800,
                                    color: badgeColor,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 8),
                              const Icon(
                                Icons.more_vert_rounded,
                                color: Color(0xFF94A3B8),
                                size: 20,
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
                error: (e, _) => Center(child: Text('Error loading users: $e')),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
