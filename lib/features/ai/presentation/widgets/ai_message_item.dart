import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../../../core/widgets/common_widgets/feedback/leo_snack_bar.dart';
import '../../domain/entities/ai_message.dart';
import 'ai_thinking_box.dart';

class AiMessageItem extends StatelessWidget {
  const AiMessageItem({super.key, required this.message});

  final AiMessage message;

  void _copyToClipboard(BuildContext context) {
    Clipboard.setData(ClipboardData(text: message.content));
    LeoSnackBar.showSuccess(context, 'Copied response to clipboard');
  }

  @override
  Widget build(BuildContext context) {
    if (message.isUser) {
      return Align(
        alignment: Alignment.centerRight,
        child: Container(
          constraints: const BoxConstraints(maxWidth: 540),
          margin: const EdgeInsets.symmetric(vertical: 6),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: BoxDecoration(
            color: LeoColors.primary,
            borderRadius: BorderRadius.circular(20),
          ),
          child: Text(
            message.content,
            style: const TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w500,
              color: Colors.white,
              height: 1.4,
            ),
          ),
        ),
      );
    }

    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFE2E8F0)),
        boxShadow: const [
          BoxShadow(
            color: Color(0x08000000),
            blurRadius: 10,
            offset: Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header with AI Model Badge & Actions
          Row(
            children: [
              Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [Color(0xFFF97316), Color(0xFFF43F5E)],
                  ),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(
                  Icons.auto_awesome,
                  color: Colors.white,
                  size: 16,
                ),
              ),
              const SizedBox(width: 10),
              const Text(
                'Leo AI',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w800,
                  color: LeoColors.textPrimary,
                ),
              ),
              const SizedBox(width: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: LeoColors.primarySoft,
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  message.modelId ?? 'leo-ultra',
                  style: const TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.w700,
                    color: LeoColors.primary,
                  ),
                ),
              ),
              const Spacer(),
              LeoPressable(
                onPressed: () => _copyToClipboard(context),
                child: const Padding(
                  padding: EdgeInsets.all(4),
                  child: Icon(
                    Icons.copy_rounded,
                    size: 18,
                    color: Color(0xFF94A3B8),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),

          // Thinking Box (if any)
          if (message.hasThinking)
            AiThinkingBox(
              thinkingContent: message.thinkingContent!,
              durationSecs: message.thinkingDurationSecs,
            ),

          // Message Content
          SelectableText(
            message.content,
            style: const TextStyle(
              fontSize: 14,
              height: 1.55,
              color: Color(0xFF1E293B),
            ),
          ),
        ],
      ),
    );
  }
}
