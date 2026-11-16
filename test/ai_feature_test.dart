import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/ai/domain/entities/ai_model.dart';
import 'package:leochat/features/ai/presentation/pages/ai_page.dart';
import 'package:leochat/features/ai/presentation/providers/ai_notifier.dart';

void main() {
  group('AiNotifier Unit Tests', () {
    test('initial state contains default messages and default model', () {
      final notifier = AiNotifier();
      expect(notifier.state.selectedModel.id, equals('leo-ultra'));
      expect(notifier.state.messages.length, greaterThanOrEqualTo(2));
      expect(notifier.state.isGenerating, isFalse);
    });

    test('selectModel updates selectedModel in state', () {
      final notifier = AiNotifier();
      final reasoner = AiModelInfo.models.firstWhere(
        (m) => m.id == 'leo-reasoner',
      );
      notifier.selectModel(reasoner);
      expect(notifier.state.selectedModel.id, equals('leo-reasoner'));
      expect(notifier.state.selectedModel.name, equals('Leo Reasoning R1'));
    });

    test('clearHistory empties the messages list', () {
      final notifier = AiNotifier();
      expect(notifier.state.messages, isNotEmpty);
      notifier.clearHistory();
      expect(notifier.state.messages, isEmpty);
    });
  });

  group('AiPage Widget Tests', () {
    testWidgets('renders Leo AI title, model button, and composer',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: AiPage(),
          ),
        ),
      );

      expect(find.text('LeoChat 4.5 Ultra'), findsOneWidget);
      expect(find.text('Ask Leo AI anything...'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);
    });
  });
}
