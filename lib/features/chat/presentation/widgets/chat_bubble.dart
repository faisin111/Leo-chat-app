import 'package:flutter/material.dart';

import '../../../../core/theme/colors.dart';
import '../../domain/entities/message.dart';
import 'voice_message_player.dart';

class LeoChatBubble extends StatelessWidget {
  const LeoChatBubble({
    super.key,
    required this.message,
    this.onRetry,
  });

  final Message message;
  final VoidCallback? onRetry;

  String _formatTime(DateTime? date) {
    if (date == null) return '';
    final hour =
        date.hour > 12 ? date.hour - 12 : (date.hour == 0 ? 12 : date.hour);
    final minute = date.minute.toString().padLeft(2, '0');
    return '$hour:$minute';
  }

  @override
  Widget build(BuildContext context) {
    final isOwn = message.isOwn;
    final isDeleted = message.deleted;
    final timeStr = _formatTime(message.createdAt);

    final bubble = isOwn
        ? _buildOutgoingBubble(context, timeStr, isDeleted)
        : _buildIncomingBubble(context, timeStr, isDeleted);

    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: 0.0, end: 1.0),
      duration: const Duration(milliseconds: 220),
      curve: Curves.easeOutCubic,
      builder: (context, value, child) {
        return Transform.scale(
          scale: 0.96 + (0.04 * value),
          alignment: isOwn ? Alignment.bottomRight : Alignment.bottomLeft,
          child: Opacity(
            opacity: value.clamp(0.0, 1.0),
            child: child,
          ),
        );
      },
      child: bubble,
    );
  }

  Widget _buildOutgoingBubble(
      BuildContext context, String timeStr, bool isDeleted) {
    final isFailed = message.sendStatus == MessageSendStatus.failed;
    final isSending = message.sendStatus == MessageSendStatus.sending;

    return Align(
      alignment: Alignment.centerRight,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Container(
              constraints: const BoxConstraints(maxWidth: 520),
              padding: const EdgeInsets.fromLTRB(18, 14, 18, 12),
              decoration: const BoxDecoration(
                color: LeoColors.primary,
                borderRadius: BorderRadius.all(Radius.circular(22)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  if (message.replyTo != null && !isDeleted)
                    _buildReplyQuote(message.replyTo!, isOwn: true),
                  if (isDeleted)
                    const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.delete_outline,
                            size: 15, color: Colors.white70),
                        SizedBox(width: 6),
                        Text(
                          'This message was deleted',
                          style: TextStyle(
                            fontStyle: FontStyle.italic,
                            fontSize: 14,
                            color: Colors.white70,
                          ),
                        ),
                      ],
                    )
                  else if (_isVoiceMessage)
                    _buildVoiceContent(isOwn: true)
                  else if (_isImageMessage)
                    _buildImageContent()
                  else
                    Text(
                      message.content ?? '',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        height: 1.35,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  const SizedBox(height: 5),
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        timeStr,
                        style: const TextStyle(
                          color: Colors.white70,
                          fontSize: 11,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(width: 4),
                      if (isSending)
                        const SizedBox(
                          width: 10,
                          height: 10,
                          child: CircularProgressIndicator(
                            strokeWidth: 1.5,
                            valueColor:
                                AlwaysStoppedAnimation<Color>(Colors.white70),
                          ),
                        )
                      else if (!isFailed)
                        Icon(
                          message.isRead ? Icons.done_all : Icons.done,
                          size: 14,
                          color: message.isRead ? Colors.white : Colors.white70,
                        ),
                    ],
                  ),
                ],
              ),
            ),
            if (isFailed)
              GestureDetector(
                onTap: onRetry,
                child: const Padding(
                  padding: EdgeInsets.only(top: 6),
                  child: Text(
                    'ⓘ  Not sent · Tap to retry',
                    style: TextStyle(
                      color: Color(0xFFEF4444),
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
            if (message.reactions.isNotEmpty && !isDeleted)
              _buildReactionsRow(message.reactions),
          ],
        ),
      ),
    );
  }

  Widget _buildIncomingBubble(
      BuildContext context, String timeStr, bool isDeleted) {
    final isVoice = _isVoiceMessage;

    return Align(
      alignment: Alignment.centerLeft,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.only(left: 4, bottom: 4),
              child: Text(
                message.effectiveSenderName,
                style: const TextStyle(
                  color: LeoColors.primary,
                  fontSize: 14,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ),
            Container(
              constraints: const BoxConstraints(maxWidth: 520),
              padding: const EdgeInsets.fromLTRB(16, 14, 16, 12),
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.all(Radius.circular(22)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (message.replyTo != null && !isDeleted)
                    _buildReplyQuote(message.replyTo!, isOwn: false),
                  if (isDeleted)
                    const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.delete_outline,
                            size: 15, color: Color(0xFF94A3B8)),
                        SizedBox(width: 6),
                        Text(
                          'This message was deleted',
                          style: TextStyle(
                            fontStyle: FontStyle.italic,
                            fontSize: 14,
                            color: Color(0xFF94A3B8),
                          ),
                        ),
                      ],
                    )
                  else if (isVoice)
                    _buildVoiceContent(isOwn: false)
                  else if (_isImageMessage)
                    _buildImageContent()
                  else
                    Text(
                      message.content ?? '',
                      style: const TextStyle(
                        color: LeoColors.textPrimary,
                        fontSize: 16,
                        height: 1.35,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  const SizedBox(height: 5),
                  Align(
                    alignment: Alignment.centerRight,
                    child: Text(
                      timeStr,
                      style: const TextStyle(
                        color: LeoColors.textMuted,
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            if (message.reactions.isNotEmpty && !isDeleted)
              _buildReactionsRow(message.reactions),
          ],
        ),
      ),
    );
  }

  bool get _isImageMessage {
    final normalizedType = message.type.trim().toUpperCase();
    return (normalizedType == 'IMAGE' ||
            normalizedType == 'PHOTO' ||
            normalizedType == 'IMAGE_MESSAGE') &&
        (message.content?.trim().isNotEmpty ?? false);
  }

  bool get _isVoiceMessage {
    final normalizedType = message.type.trim().toUpperCase();
    return normalizedType == 'VOICE' || normalizedType == 'AUDIO';
  }

  Widget _buildVoiceContent({required bool isOwn}) {
    return LeoVoiceMessagePlayer(
      isOwn: isOwn,
      audioUrl: message.content,
    );
  }

  Widget _buildImageContent() {
    return ClipRRect(
      borderRadius: BorderRadius.circular(14),
      child: Image.network(
        message.content!.trim(),
        width: 280,
        height: 200,
        fit: BoxFit.cover,
        loadingBuilder: (context, child, progress) => progress == null
            ? child
            : const SizedBox(
                width: 280,
                height: 200,
                child: Center(
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
              ),
        errorBuilder: (context, error, stackTrace) => Container(
          width: 280,
          height: 120,
          color: const Color(0xFFF3ECE5),
          alignment: Alignment.center,
          child: const Icon(
            Icons.broken_image_outlined,
            color: LeoColors.textMuted,
            size: 32,
          ),
        ),
      ),
    );
  }

  Widget _buildReplyQuote(MessageReply reply, {required bool isOwn}) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
      decoration: BoxDecoration(
        color: isOwn ? Colors.black.withOpacity(0.12) : const Color(0xFFFFEEE3),
        borderRadius: BorderRadius.circular(12),
        border: Border(
          left: BorderSide(
            color: isOwn ? Colors.white70 : LeoColors.primary,
            width: 3,
          ),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            reply.senderDisplayName ?? reply.senderUsername ?? 'User',
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w800,
              color: isOwn ? Colors.white : LeoColors.primary,
            ),
          ),
          const SizedBox(height: 2),
          Text(
            reply.content ?? '',
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w500,
              color: isOwn ? Colors.white70 : LeoColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildReactionsRow(List<MessageReaction> reactions) {
    return Padding(
      padding: const EdgeInsets.only(top: 4, left: 4),
      child: Wrap(
        spacing: 6,
        children: reactions.map((r) {
          return Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: const Color(0xFFE2E8F0)),
            ),
            child: Text(
              '${r.emoji} ${r.count}',
              style: const TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.bold,
              ),
            ),
          );
        }).toList(),
      ),
    );
  }
}
