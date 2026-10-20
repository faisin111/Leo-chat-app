import 'package:flutter/material.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import 'shared_media_page.dart';

class GroupDetailsPage extends StatefulWidget {
  const GroupDetailsPage({
    super.key,
    this.groupName = 'Weekend people',
    this.groupDescription = 'Less scrolling, more exploring. ☀️',
  });

  final String groupName;
  final String groupDescription;

  @override
  State<GroupDetailsPage> createState() => _GroupDetailsPageState();
}

class _GroupDetailsPageState extends State<GroupDetailsPage> {
  bool _isMuted = false;

  final _members = [
    const _GroupMember(
      name: 'You',
      avatarUrl: 'assets/avatars/alex.png',
      badge: 'Group owner',
    ),
    const _GroupMember(
      name: 'Maya Chen',
      avatarUrl: 'assets/avatars/maya.png',
      badge: 'Admin',
    ),
    const _GroupMember(
      name: 'Noah Williams',
      avatarUrl: 'assets/avatars/noah.png',
      hasMoreAction: true,
    ),
    const _GroupMember(
      name: 'Sofia Rivera',
      avatarUrl: 'assets/avatars/sofia.png',
      hasMoreAction: true,
    ),
    const _GroupMember(
      name: 'Ellie Park',
      avatarUrl: 'assets/avatars/ellie.png',
      hasMoreAction: true,
    ),
    const _GroupMember(
      name: 'Jamie Lee',
      fallbackText: 'JL',
      canRemove: true,
    ),
  ];

  @override
  Widget build(BuildContext context) {
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
        actions: [
          LeoPressable(
            child: IconButton(
              icon: const Icon(Icons.edit_outlined,
                  color: LeoColors.textPrimary, size: 24),
              onPressed: () {},
            ),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // Hero Sun Gradient Squircle
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
                  Icons.wb_sunny_outlined,
                  color: Colors.white,
                  size: 38,
                ),
              ),
              const SizedBox(height: 14),

              Text(
                widget.groupName,
                style: const TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.w900,
                  letterSpacing: -0.5,
                  color: LeoColors.textPrimary,
                ),
              ),
              const SizedBox(height: 4),

              Text(
                widget.groupDescription,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFF78716C),
                ),
              ),
              const SizedBox(height: 4),

              const Text(
                'Created by you · Oct 12, 2026',
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                  color: Color(0xFFA8A19B),
                ),
              ),
              const SizedBox(height: 20),

              // 3 Quick Action Buttons
              Row(
                children: [
                  Expanded(
                    child: _QuickActionButton(
                      icon: Icons.phone_outlined,
                      label: 'Call',
                      onTap: () {
                        LeoSnackBar.showSuccess(
                            context, 'Starting group call...');
                      },
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _QuickActionButton(
                      icon: Icons.search_rounded,
                      label: 'Search',
                      onTap: () {},
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _QuickActionButton(
                      icon: _isMuted
                          ? Icons.notifications_off_rounded
                          : Icons.notifications_off_outlined,
                      label: _isMuted ? 'Muted' : 'Mute',
                      onTap: () {
                        setState(() => _isMuted = !_isMuted);
                        LeoSnackBar.showSuccess(
                          context,
                          _isMuted
                              ? 'Notifications muted'
                              : 'Notifications unmuted',
                        );
                      },
                    ),
                  ),
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
                      trailingText: '24',
                      onTap: () {
                        Navigator.of(context).push(
                          MaterialPageRoute<void>(
                            builder: (_) => const SharedMediaPage(
                              conversationTitle: 'Weekend people',
                            ),
                          ),
                        );
                      },
                    ),
                    const Divider(
                        height: 1, indent: 64, color: Color(0xFFF3ECE5)),
                    _DetailRow(
                      icon: Icons.link_rounded,
                      title: 'Group invite link',
                      onTap: () {
                        LeoSnackBar.showSuccess(
                            context, 'Invite link copied to clipboard');
                      },
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),

              // Section: Members · 6
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Members · ${_members.length}',
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                      color: LeoColors.textPrimary,
                    ),
                  ),
                  LeoPressable(
                    onPressed: () {
                      LeoSnackBar.showSuccess(
                          context, 'Select contacts to add');
                    },
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

              // Members List
              Container(
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(color: const Color(0xFFE7DFD8)),
                ),
                child: ListView.separated(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: _members.length,
                  separatorBuilder: (_, __) => const Divider(
                      height: 1, indent: 68, color: Color(0xFFF3ECE5)),
                  itemBuilder: (context, index) {
                    final member = _members[index];
                    return Padding(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 12),
                      child: Row(
                        children: [
                          LeoAvatar(
                            radius: 22,
                            imageUrl: member.avatarUrl,
                            assetPath: member.avatarUrl,
                            fallbackText: member.fallbackText,
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Text(
                              member.name,
                              style: const TextStyle(
                                fontSize: 15,
                                fontWeight: FontWeight.w800,
                                color: LeoColors.textPrimary,
                              ),
                            ),
                          ),
                          if (member.badge != null)
                            Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 12, vertical: 4),
                              decoration: BoxDecoration(
                                color: const Color(0xFFFFEEE3),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Text(
                                member.badge!,
                                style: const TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w800,
                                  color: Color(0xFFC05621),
                                ),
                              ),
                            ),
                          if (member.hasMoreAction)
                            IconButton(
                              icon: const Icon(Icons.more_horiz_rounded,
                                  color: Color(0xFF78716C)),
                              onPressed: () {},
                            ),
                          if (member.canRemove)
                            LeoPressable(
                              onPressed: () {
                                LeoSnackBar.showSuccess(
                                    context, 'Removed ${member.name}');
                              },
                              child: const Text(
                                'Remove',
                                style: TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.w800,
                                  color: Color(0xFFDC2626),
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

              // Leave group button
              LeoPressable(
                onPressed: () {
                  Navigator.of(context).pop();
                },
                child: const Padding(
                  padding: EdgeInsets.symmetric(vertical: 8),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.logout_rounded,
                          color: Color(0xFFDC2626), size: 20),
                      SizedBox(width: 8),
                      Text(
                        'Leave group',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                          color: Color(0xFFDC2626),
                        ),
                      ),
                    ],
                  ),
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

class _GroupMember {
  const _GroupMember({
    required this.name,
    this.avatarUrl,
    this.fallbackText,
    this.badge,
    this.hasMoreAction = false,
    this.canRemove = false,
  });

  final String name;
  final String? avatarUrl;
  final String? fallbackText;
  final String? badge;
  final bool hasMoreAction;
  final bool canRemove;
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
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: const Color(0xFFE7DFD8)),
        ),
        child: Column(
          children: [
            Icon(icon, color: LeoColors.primary, size: 22),
            const SizedBox(height: 6),
            Text(
              label,
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w800,
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
    this.trailingText,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String? trailingText;
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
              child: Icon(icon, color: LeoColors.primary, size: 22),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Text(
                title,
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w800,
                  color: LeoColors.textPrimary,
                ),
              ),
            ),
            if (trailingText != null) ...[
              Text(
                trailingText!,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: Color(0xFF78716C),
                ),
              ),
              const SizedBox(width: 6),
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
