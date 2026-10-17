import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import 'package:leochat/features/chat/presentation/widgets/chat_bubble.dart';

void main() {
  testWidgets('image messages render their network image URL', (tester) async {
    const message = Message(
      id: 'image-1',
      conversationId: 'conversation-1',
      type: 'IMAGE',
      content: 'https://example.com/photo.png',
      isOwn: false,
    );

    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(body: LeoChatBubble(message: message)),
      ),
    );

    expect(find.byType(Image), findsOneWidget);
    final image = tester.widget<Image>(find.byType(Image));
    expect((image.image as NetworkImage).url, 'https://example.com/photo.png');
  });
}
