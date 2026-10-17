import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/material.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import 'package:leochat/features/chat/presentation/widgets/chat_bubble.dart';

void main() {
  testWidgets('audio messages render the voice message control',
      (tester) async {
    const message = Message(
      id: 'voice-1',
      conversationId: 'conversation-1',
      type: 'AUDIO',
      content: 'https://example.com/voice.wav',
      isOwn: true,
    );

    await tester.pumpWidget(const MaterialApp(
      home: Scaffold(body: LeoChatBubble(message: message)),
    ));

    expect(find.byIcon(Icons.play_arrow_rounded), findsOneWidget);
    expect(find.textContaining('0:18'), findsOneWidget);
  });
}
