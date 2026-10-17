import 'package:flutter/material.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/branding/leo_avatar.dart';
import '../../domain/entities/conversation.dart';

class LeoConversationTile extends StatelessWidget {
  const LeoConversationTile({
    super.key,
    required this.conversation,
    required this.isSelected,
    required this.onTap,
  });

  final Conversation conversation;
  final bool isSelected;
  final VoidCallback onTap;

  String _formatTime(DateTime? date) {
    if (date == null) return '';
    final now = DateTime.now();
    final diff = now.difference(date);
    if (diff.inDays > 1) {
      return '${date.month}/${date.day}';
    } else if (diff.inDays == 1) {
      return 'Yesterday';
    } else {
      final hour =
          date.hour > 12 ? date.hour - 12 : (date.hour == 0 ? 12 : date.hour);
      final minute = date.minute.toString().padLeft(2, '0');
      return '$hour:$minute';
    }
  }

  String _initials(String value) {
    final words = value.trim().split(RegExp(r'\s+'))
      ..removeWhere((word) => word.isEmpty);
    if (words.isEmpty) return '?';
    if (words.length == 1) return words.first.substring(0, 1).toUpperCase();
    return '${words.first.substring(0, 1)}${words.last.substring(0, 1)}'
        .toUpperCase();
  }

  @override
  Widget build(BuildContext context) {
    final title = conversation.effectiveTitle;
    final isTyping = conversation.isTyping;
    final snippet = isTyping
        ? 'typing...'
        : (conversation.lastMessageSnippet ?? 'No messages yet');
    final timeStr = conversation.lastMessageAt != null
        ? _formatTime(conversation.lastMessageAt)
        : (conversation.createdAt != null
            ? _formatTime(conversation.createdAt)
            : '');

    return Material(
      color: isSelected ? const Color(0xFFF3ECE5) : Colors.transparent,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
          child: Row(
            children: [
              if (conversation.isGroup &&
                  conversation.preferredAvatarUrl == null)
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
                    child: const Icon(Icons.group_rounded,
                        color: Colors.white, size: 24))
              else
                LeoAvatar(
                    radius: 26,
                    imageUrl: conversation.preferredAvatarUrl,
                    fallbackText: _initials(title),
                    hasOnlineBadge:
                        !conversation.isGroup && conversation.isOnline),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                        color: Color(0xFF1C1917),
                      ),
                    ),
                    const SizedBox(height: 3),
                    Text(
                      snippet,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight:
                            isTyping ? FontWeight.w700 : FontWeight.w500,
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
                    timeStr,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: conversation.unreadCount > 0
                          ? FontWeight.w700
                          : FontWeight.w600,
                      color: conversation.unreadCount > 0
                          ? LeoColors.primary
                          : const Color(0xFF78716C),
                    ),
                  ),
                  const SizedBox(height: 4),
                  if (conversation.unreadCount > 0)
                    Container(
                      width: 20,
                      height: 20,
                      decoration: const BoxDecoration(
                        color: LeoColors.primary,
                        shape: BoxShape.circle,
                      ),
                      child: Center(
                        child: Text(
                          '${conversation.unreadCount}',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 11,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                    )
                  else if (conversation.isMuted)
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
        ),
      ),
    );
  }
}
