import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/di/providers.dart';
import '../../../chat/domain/repositories/chat_repository.dart';
import '../../domain/entities/ai_message.dart';
import '../../domain/entities/ai_model.dart';
import '../../domain/repositories/ai_repository.dart';
import 'ai_state.dart';

class AiNotifier extends StateNotifier<AiState> {
  AiNotifier([this._aiRepository, this._chatRepository])
      : super(const AiState()) {
    if (_aiRepository != null) {
      initConversation();
    }
  }

  final AiRepository? _aiRepository;
  final ChatRepository? _chatRepository;

  Future<void> initConversation() async {
    final aiRepo = _aiRepository;
    if (aiRepo == null) return;

    final result = await aiRepo.getOrCreateConversation();
    result.fold(
      (failure) {
        state = state.copyWith(errorMessage: failure.message);
      },
      (conv) async {
        state = state.copyWith(conversationId: conv.id, clearError: true);
        final chatRepo = _chatRepository;
        if (chatRepo != null && state.messages.isEmpty) {
          final historyResult = await chatRepo.getMessages(
            conversationId: conv.id,
            limit: 50,
          );
          historyResult.fold(
            (_) {},
            (messages) {
              if (messages.isNotEmpty) {
                final mapped = messages.map((m) {
                  return AiMessage(
                    id: m.id,
                    role: m.aiGenerated
                        ? AiMessageRole.assistant
                        : AiMessageRole.user,
                    content: m.content ?? '',
                    createdAt: m.createdAt ?? DateTime.now(),
                  );
                }).toList();
                state = state.copyWith(messages: mapped);
              }
            },
          );
        }
      },
    );
  }

  void selectModel(AiModelInfo model) {
    state = state.copyWith(selectedModel: model);
  }

  void toggleDeepThink() {
    state = state.copyWith(isDeepThinkEnabled: !state.isDeepThinkEnabled);
  }

  void toggleWebSearch() {
    state = state.copyWith(isWebSearchEnabled: !state.isWebSearchEnabled);
  }

  Future<void> clearHistory() async {
    if (_aiRepository != null) {
      await deleteConversation();
    } else {
      state = state.copyWith(messages: []);
    }
  }

  Future<void> deleteConversation() async {
    final aiRepo = _aiRepository;
    if (aiRepo == null) {
      state = state.copyWith(
        isGenerating: false,
        messages: [],
        clearError: true,
        clearPendingAction: true,
      );
      return;
    }
    state = state.copyWith(isGenerating: true);
    final result = await aiRepo.deleteConversation();
    result.fold(
      (failure) {
        state = state.copyWith(
          isGenerating: false,
          errorMessage: failure.message,
        );
      },
      (_) {
        state = state.copyWith(
          isGenerating: false,
          messages: [],
          clearError: true,
          clearPendingAction: true,
        );
      },
    );
  }

  Future<void> sendMessage(String text) async {
    final prompt = text.trim();
    if (prompt.isEmpty || state.isGenerating) return;

    final clientMessageId = 'ai-client-${DateTime.now().millisecondsSinceEpoch}';
    final userMsg = AiMessage(
      id: clientMessageId,
      role: AiMessageRole.user,
      content: prompt,
      createdAt: DateTime.now(),
    );

    final updated = List<AiMessage>.from(state.messages)..add(userMsg);
    state = state.copyWith(
      messages: updated,
      isGenerating: true,
      clearError: true,
    );

    final aiRepo = _aiRepository;
    if (aiRepo == null) {
      final assistantMsg = AiMessage(
        id: 'reply-${DateTime.now().millisecondsSinceEpoch}',
        role: AiMessageRole.assistant,
        content: 'I have received your message: "$prompt"',
        createdAt: DateTime.now(),
      );
      state = state.copyWith(
        messages: List<AiMessage>.from(state.messages)..add(assistantMsg),
        isGenerating: false,
      );
      return;
    }

    String? activeConvId = state.conversationId;
    if (activeConvId == null || activeConvId.isEmpty) {
      final convRes = await aiRepo.getOrCreateConversation();
      convRes.fold(
        (_) {},
        (c) {
          activeConvId = c.id;
          state = state.copyWith(conversationId: c.id);
        },
      );
    }

    final targetConvId = activeConvId;
    if (targetConvId == null || targetConvId.isEmpty) {
      final errorMsg = AiMessage(
        id: 'err-${DateTime.now().millisecondsSinceEpoch}',
        role: AiMessageRole.assistant,
        content: '⚠️ **Server error:** Could not establish AI conversation.',
        createdAt: DateTime.now(),
      );
      state = state.copyWith(
        messages: List<AiMessage>.from(state.messages)..add(errorMsg),
        isGenerating: false,
      );
      return;
    }

    final chatResult = await aiRepo.chat(
      conversationId: targetConvId,
      prompt: prompt,
      clientMessageId: clientMessageId,
    );

    chatResult.fold(
      (failure) {
        final errorMsg = AiMessage(
          id: 'err-${DateTime.now().millisecondsSinceEpoch}',
          role: AiMessageRole.assistant,
          content: '⚠️ **Server error:** ${failure.message}',
          createdAt: DateTime.now(),
        );
        state = state.copyWith(
          messages: List<AiMessage>.from(state.messages)..add(errorMsg),
          isGenerating: false,
          errorMessage: failure.message,
        );
      },
      (msg) {
        final assistantMsg = AiMessage(
          id: msg.id,
          role: AiMessageRole.assistant,
          modelId: state.selectedModel.id,
          content: msg.content ?? '',
          createdAt: msg.createdAt ?? DateTime.now(),
        );
        state = state.copyWith(
          messages: List<AiMessage>.from(state.messages)..add(assistantMsg),
          isGenerating: false,
        );
      },
    );
  }

  Future<void> executeAction(Map<String, dynamic> request) async {
    final aiRepo = _aiRepository;
    if (aiRepo == null) return;
    state = state.copyWith(isGenerating: true);
    final result = await aiRepo.executeAction(request);
    result.fold(
      (failure) {
        state = state.copyWith(
          isGenerating: false,
          errorMessage: failure.message,
        );
      },
      (data) {
        final confirmationRequired = data['confirmationRequired'] == true;
        if (confirmationRequired) {
          state = state.copyWith(
            isGenerating: false,
            pendingAction: request,
          );
        } else {
          final actionName = data['action']?.toString() ?? 'Action';
          final message = data['message']?.toString() ?? 'Completed';
          final actionMsg = AiMessage(
            id: 'act-${DateTime.now().millisecondsSinceEpoch}',
            role: AiMessageRole.assistant,
            content: '✅ **$actionName**: $message\n`${data['data']}`',
            createdAt: DateTime.now(),
          );
          state = state.copyWith(
            isGenerating: false,
            messages: List<AiMessage>.from(state.messages)..add(actionMsg),
            clearPendingAction: true,
          );
        }
      },
    );
  }

  Future<void> confirmPendingAction() async {
    if (state.pendingAction == null) return;
    final confirmedRequest = Map<String, dynamic>.from(state.pendingAction!)
      ..['confirmed'] = true;
    await executeAction(confirmedRequest);
  }

  void cancelPendingAction() {
    state = state.copyWith(clearPendingAction: true);
  }
}

final aiNotifierProvider =
    StateNotifierProvider<AiNotifier, AiState>((ref) {
  final aiRepo = ref.watch(aiRepositoryProvider);
  final chatRepo = ref.watch(chatRepositoryProvider);
  return AiNotifier(aiRepo, chatRepo);
});
