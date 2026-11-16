import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/colors.dart';
import '../../../../core/widgets/common_widgets/buttons/leo_pressable.dart';
import '../../domain/entities/ai_model.dart';
import '../providers/ai_notifier.dart';
import '../widgets/ai_message_item.dart';

class AiPage extends ConsumerStatefulWidget {
  const AiPage({super.key});

  @override
  ConsumerState<AiPage> createState() => _AiPageState();
}

class _AiPageState extends ConsumerState<AiPage> {
  final _inputController = TextEditingController();
  final _scrollController = ScrollController();

  static const _suggestionPrompts = [
    'Explain STOMP sequence deduplication in chat architecture',
    'Write a Flutter widget for smooth real-time voice message player',
    'How does single-flight token refresh interceptor prevent 401 storms?',
    'Design an end-to-end database schema for a high scale chat app',
  ];

  @override
  void dispose() {
    _inputController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOutCubic,
        );
      }
    });
  }

  void _handleSend([String? presetText]) {
    final text = presetText ?? _inputController.text;
    if (text.trim().isEmpty) return;

    ref.read(aiNotifierProvider.notifier).sendMessage(text);
    _inputController.clear();
    _scrollToBottom();
  }

  void _showModelPicker(BuildContext context, AiModelInfo currentModel) {
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) {
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
                const Text(
                  'Select Intelligence Model',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w800,
                    color: LeoColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                const Text(
                  'Switch between specialized reasoning engines',
                  style: TextStyle(
                    fontSize: 13,
                    color: LeoColors.textSecondary,
                  ),
                ),
                const SizedBox(height: 16),
                ...AiModelInfo.models.map((model) {
                  final isSelected = model.id == currentModel.id;
                  return LeoPressable(
                    onPressed: () {
                      ref.read(aiNotifierProvider.notifier).selectModel(model);
                      Navigator.pop(context);
                    },
                    child: Container(
                      margin: const EdgeInsets.only(bottom: 8),
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: isSelected
                            ? LeoColors.primarySoft
                            : const Color(0xFFF8FAFC),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: isSelected
                              ? LeoColors.primary
                              : const Color(0xFFE2E8F0),
                        ),
                      ),
                      child: Row(
                        children: [
                          Icon(
                            _getIconData(model.icon),
                            color: isSelected
                                ? LeoColors.primary
                                : const Color(0xFF64748B),
                            size: 22,
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Row(
                                  children: [
                                    Text(
                                      model.name,
                                      style: TextStyle(
                                        fontSize: 15,
                                        fontWeight: FontWeight.w700,
                                        color: isSelected
                                            ? LeoColors.primary
                                            : LeoColors.textPrimary,
                                      ),
                                    ),
                                    const SizedBox(width: 6),
                                    Container(
                                      padding: const EdgeInsets.symmetric(
                                          horizontal: 6, vertical: 1.5),
                                      decoration: BoxDecoration(
                                        color: isSelected
                                            ? LeoColors.primary
                                            : const Color(0xFFE2E8F0),
                                        borderRadius: BorderRadius.circular(4),
                                      ),
                                      child: Text(
                                        model.badge,
                                        style: TextStyle(
                                          fontSize: 9,
                                          fontWeight: FontWeight.w800,
                                          color: isSelected
                                              ? Colors.white
                                              : const Color(0xFF475569),
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  model.tagline,
                                  style: const TextStyle(
                                    fontSize: 12,
                                    color: LeoColors.textSecondary,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          if (isSelected)
                            const Icon(
                              Icons.check_circle_rounded,
                              color: LeoColors.primary,
                              size: 20,
                            ),
                        ],
                      ),
                    ),
                  );
                }),
              ],
            ),
          ),
        );
      },
    );
  }

  IconData _getIconData(AiModelIcon icon) {
    switch (icon) {
      case AiModelIcon.sparkles:
        return Icons.auto_awesome;
      case AiModelIcon.brain:
        return Icons.psychology_rounded;
      case AiModelIcon.code:
        return Icons.code_rounded;
      case AiModelIcon.zap:
        return Icons.bolt_rounded;
    }
  }

  @override
  Widget build(BuildContext context) {
    final aiState = ref.watch(aiNotifierProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFFAF7F4),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        scrolledUnderElevation: 1,
        title: LeoPressable(
          onPressed: () => _showModelPicker(context, aiState.selectedModel),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                decoration: BoxDecoration(
                  color: LeoColors.primarySoft,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: LeoColors.primary.withOpacity(0.3)),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      _getIconData(aiState.selectedModel.icon),
                      color: LeoColors.primary,
                      size: 16,
                    ),
                    const SizedBox(width: 6),
                    Text(
                      aiState.selectedModel.name,
                      style: const TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                        color: LeoColors.primary,
                      ),
                    ),
                    const SizedBox(width: 4),
                    const Icon(
                      Icons.keyboard_arrow_down_rounded,
                      color: LeoColors.primary,
                      size: 18,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        actions: [
          IconButton(
            tooltip: 'Clear history',
            icon: const Icon(Icons.delete_sweep_outlined,
                color: LeoColors.textSecondary),
            onPressed: () =>
                ref.read(aiNotifierProvider.notifier).clearHistory(),
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Messages area
            Expanded(
              child: aiState.messages.isEmpty
                  ? _buildEmptyState()
                  : ListView.builder(
                      controller: _scrollController,
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 12),
                      itemCount: aiState.messages.length +
                          (aiState.isGenerating ? 1 : 0),
                      itemBuilder: (context, index) {
                        if (index < aiState.messages.length) {
                          return AiMessageItem(
                            message: aiState.messages[index],
                          );
                        }
                        return _buildGeneratingIndicator();
                      },
                    ),
            ),

            // Prompt suggestions chips (when short history)
            if (aiState.messages.length <= 2 && !aiState.isGenerating)
              SizedBox(
                height: 38,
                child: ListView.separated(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  scrollDirection: Axis.horizontal,
                  itemCount: _suggestionPrompts.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 8),
                  itemBuilder: (context, index) {
                    final prompt = _suggestionPrompts[index];
                    return LeoPressable(
                      onPressed: () => _handleSend(prompt),
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 12, vertical: 8),
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(color: const Color(0xFFE2E8F0)),
                        ),
                        child: Text(
                          prompt,
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: Color(0xFF475569),
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),

            // Composer bar
            Container(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
              decoration: const BoxDecoration(
                color: Colors.white,
                border: Border(top: BorderSide(color: Color(0xFFE2E8F0))),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: Container(
                      decoration: BoxDecoration(
                        color: const Color(0xFFF1F5F9),
                        borderRadius: BorderRadius.circular(24),
                      ),
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      child: TextField(
                        controller: _inputController,
                        textInputAction: TextInputAction.send,
                        onSubmitted: (_) => _handleSend(),
                        decoration: const InputDecoration(
                          hintText: AppStrings.askAiAnything,
                          hintStyle: TextStyle(
                            fontSize: 14,
                            color: Color(0xFF94A3B8),
                          ),
                          border: InputBorder.none,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 10),
                  LeoPressable(
                    onPressed:
                        aiState.isGenerating ? null : () => _handleSend(),
                    child: Container(
                      width: 44,
                      height: 44,
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [Color(0xFFF97316), Color(0xFFF43F5E)],
                        ),
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: const Color(0xFFF97316).withOpacity(0.35),
                            blurRadius: 10,
                            offset: const Offset(0, 3),
                          ),
                        ],
                      ),
                      child: const Icon(
                        Icons.arrow_upward_rounded,
                        color: Colors.white,
                        size: 22,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFFF97316), Color(0xFFF43F5E)],
                ),
                borderRadius: BorderRadius.circular(24),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFFF97316).withOpacity(0.3),
                    blurRadius: 20,
                    offset: const Offset(0, 8),
                  ),
                ],
              ),
              child: const Icon(
                Icons.auto_awesome,
                color: Colors.white,
                size: 36,
              ),
            ),
            const SizedBox(height: 20),
            const Text(
              'Leo AI Intelligence',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w800,
                color: LeoColors.textPrimary,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Ask complex architectural queries, generate code, or analyze system designs.',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 14,
                color: LeoColors.textSecondary,
                height: 1.4,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildGeneratingIndicator() {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Row(
        children: [
          Container(
            width: 24,
            height: 24,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFFF97316), Color(0xFFF43F5E)],
              ),
              borderRadius: BorderRadius.circular(6),
            ),
            child: const Icon(
              Icons.auto_awesome,
              color: Colors.white,
              size: 14,
            ),
          ),
          const SizedBox(width: 12),
          const Text(
            'Leo AI is thinking and formulating response...',
            style: TextStyle(
              fontSize: 13,
              fontStyle: FontStyle.italic,
              color: Color(0xFF64748B),
            ),
          ),
        ],
      ),
    );
  }
}
