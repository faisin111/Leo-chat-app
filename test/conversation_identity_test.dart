import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/chat/data/models/conversation_dto.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';

void main() {
  test('direct conversation identity prefers image, display name, username',
      () {
    const conversation = Conversation(
      id: '1',
      title: 'Direct conversation',
      type: 'DIRECT',
      targetDisplayName: 'Maya Chen',
      targetUsername: 'maya',
      targetAvatarUrl: 'https://example.com/maya.png',
    );

    expect(conversation.preferredAvatarUrl, 'https://example.com/maya.png');
    expect(conversation.effectiveTitle, 'Maya Chen');
  });

  test('direct conversation falls back to username when name is absent', () {
    const conversation = Conversation(
      id: '2',
      title: '',
      type: 'DIRECT',
      targetUsername: 'maya',
    );

    expect(conversation.effectiveTitle, 'maya');
  });

  test('conversation dto maps nested participant identity', () {
    final dto = ConversationDto.fromJson({
      'id': '3',
      'type': 'DIRECT',
      'targetUser': {
        'username': 'maya',
        'displayName': 'Maya Chen',
        'avatar': 'https://example.com/maya.jpg',
      },
    });

    final conversation = dto.toDomain();
    expect(conversation.effectiveTitle, 'Maya Chen');
    expect(conversation.preferredAvatarUrl, 'https://example.com/maya.jpg');
  });
}
