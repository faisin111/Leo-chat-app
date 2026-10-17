import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fpdart/fpdart.dart';
import 'package:google_fonts/google_fonts.dart';

import 'package:leochat/core/constants/app_strings.dart';
import 'package:leochat/core/di/providers.dart';
import 'package:leochat/core/network/result.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import 'package:leochat/features/chat/domain/repositories/chat_repository.dart';
import 'package:leochat/features/chat/presentation/pages/chat_shell_page.dart';
import 'package:leochat/features/chat/presentation/widgets/conversation_skeleton.dart';
import 'package:leochat/features/chat/presentation/widgets/conversation_tile.dart';
import 'package:leochat/features/chat/presentation/widgets/filter_bubble_bar.dart';
import 'package:leochat/features/chat/presentation/widgets/typing_indicator.dart';
import 'package:leochat/features/chat/presentation/widgets/voice_message_player.dart';

class _FakeChatRepository implements ChatRepository {
  _FakeChatRepository({this.delayResponse = false});

  final bool delayResponse;

  final List<Conversation> testConversations = [
    const Conversation(
      id: 'c-weekend-people-101',
      title: 'Weekend people',
      type: 'GROUP',
      lastMessageSnippet: 'Noah: This is the one ☼',
      unreadCount: 4,
      lastSeq: 5,
    ),
    const Conversation(
      id: 'c-maya-chen-102',
      title: 'Maya Chen',
      type: 'DIRECT',
      lastMessageSnippet: 'This message was deleted',
      unreadCount: 2,
      lastSeq: 2,
    ),
  ];

  final List<Message> testMessages = [
    Message(
      id: 'm-1',
      conversationId: 'c-weekend-people-101',
      senderDisplayName: 'Noah Williams',
      seq: 1,
      content: 'Saturday plan: coast, coffee, absolutely no emails.',
      createdAt: DateTime.now().subtract(const Duration(minutes: 10)),
      isOwn: false,
    ),
    Message(
      id: 'm-2',
      conversationId: 'c-weekend-people-101',
      seq: 2,
      content: 'I will book the train for all six of us.',
      createdAt: DateTime.now().subtract(const Duration(minutes: 5)),
      isOwn: true,
      sendStatus: MessageSendStatus.sent,
    ),
  ];

  @override
  Future<Result<List<Conversation>>> getConversations({
    int? cursor,
    int limit = 50,
    String? query,
  }) async {
    if (delayResponse) {
      await Future<void>.delayed(const Duration(milliseconds: 300));
    }
    if (query != null && query.isNotEmpty) {
      final filtered = testConversations
          .where((c) => c.title.toLowerCase().contains(query.toLowerCase()))
          .toList();
      return Right(filtered);
    }
    return Right(testConversations);
  }

  @override
  Future<Result<List<Message>>> getMessages({
    required String conversationId,
    int? beforeSeq,
    int? afterSeq,
    int limit = 50,
  }) async {
    return Right(testMessages);
  }

  @override
  Future<Result<Message>> sendMessage({
    required String conversationId,
    required String clientMessageId,
    required String content,
    String type = 'TEXT',
    String? replyToId,
  }) async {
    return Right(Message(
      id: 'm-persisted',
      conversationId: conversationId,
      clientMessageId: clientMessageId,
      content: content,
      seq: 3,
      isOwn: true,
      sendStatus: MessageSendStatus.sent,
    ));
  }

  @override
  Future<Result<void>> markConversationRead({
    required String conversationId,
    required int upToSeq,
  }) async =>
      const Right(null);

  @override
  Future<Result<int>> getUnreadCount() async => const Right(6);

  @override
  Future<Result<Conversation>> getConversation({
    required String conversationId,
  }) async =>
      Right(testConversations.first);
}

void main() {
  setUpAll(() {
    GoogleFonts.config.allowRuntimeFetching = false;
  });

  setUp(() {
    FlutterSecureStorage.setMockInitialValues({});
  });

  Widget createChatShell({ChatRepository? repository}) {
    return ProviderScope(
      overrides: [
        if (repository != null)
          chatRepositoryProvider.overrideWithValue(repository),
      ],
      child: const MaterialApp(
        home: ChatShellPage(),
      ),
    );
  }

  testWidgets('ChatShellPage renders header, search, and conversation tiles',
      (tester) async {
    final repo = _FakeChatRepository();
    await tester.pumpWidget(createChatShell(repository: repo));
    await tester.pumpAndSettle();

    expect(find.text('Your chats'), findsOneWidget);
    expect(find.text(AppStrings.yourChatsSubtitle), findsOneWidget);
    expect(find.text('All chats'), findsOneWidget);
    expect(find.text('Weekend people'), findsWidgets);
    expect(find.text('Maya Chen'), findsOneWidget);
  });

  testWidgets('Search input filters conversation items dynamically',
      (tester) async {
    final repo = _FakeChatRepository();
    await tester.pumpWidget(createChatShell(repository: repo));
    await tester.pumpAndSettle();

    expect(find.text('Maya Chen'), findsOneWidget);
    expect(find.text('Weekend people'), findsWidgets);

    // Type in search bar
    final searchField = find.byType(TextField).first;
    await tester.enterText(searchField, 'Maya');
    await tester.pumpAndSettle();

    expect(
        find.widgetWithText(LeoConversationTile, 'Maya Chen'), findsOneWidget);
    expect(find.widgetWithText(LeoConversationTile, 'Weekend people'),
        findsNothing);

    // Clear search
    await tester.tap(find.byIcon(Icons.close_rounded));
    await tester.pumpAndSettle();

    expect(
        find.widgetWithText(LeoConversationTile, 'Maya Chen'), findsOneWidget);
    expect(find.widgetWithText(LeoConversationTile, 'Weekend people'),
        findsOneWidget);
  });

  testWidgets('Selecting a conversation displays message history and composer',
      (tester) async {
    final repo = _FakeChatRepository();
    await tester.pumpWidget(createChatShell(repository: repo));
    await tester.pumpAndSettle();

    // Tap on 'Weekend people' tile
    await tester.tap(find.text('Weekend people').first);
    await tester.pumpAndSettle();

    // Check message detail content
    expect(find.text('Saturday plan: coast, coffee, absolutely no emails.'),
        findsOneWidget);
    expect(
        find.text('I will book the train for all six of us.'), findsOneWidget);

    // Test composer input and send
    final messageField = find.widgetWithText(TextField, 'Message...');
    expect(messageField, findsOneWidget);

    await tester.enterText(messageField, 'Count me in too!');
    await tester.pump();

    // Send button icon should now be send_rounded
    expect(find.byIcon(Icons.send_rounded), findsOneWidget);
    await tester.tap(find.byIcon(Icons.send_rounded));
    await tester.pumpAndSettle();

    expect(find.text('Count me in too!'), findsOneWidget);
  });

  testWidgets('Displays skeleton loader while loading conversations',
      (tester) async {
    final repo = _FakeChatRepository(delayResponse: true);
    await tester.pumpWidget(createChatShell(repository: repo));
    await tester.pump(); // Trigger frame before delayed future completes

    expect(find.byType(LeoConversationSkeletonList), findsOneWidget);
    await tester.pump(const Duration(milliseconds: 300));
    await tester.pumpAndSettle();

    expect(find.byType(LeoConversationSkeletonList), findsNothing);
    expect(find.text('Weekend people'), findsWidgets);
  });

  testWidgets('LeoFilterBubbleBar renders options and reacts to selection',
      (tester) async {
    int selected = 0;
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: LeoFilterBubbleBar(
            selectedIndex: selected,
            unreadCount: 5,
            onSelect: (index) => selected = index,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text(AppStrings.allChats), findsOneWidget);
    expect(find.text(AppStrings.unread), findsOneWidget);
    expect(find.text('5'), findsOneWidget);
    expect(find.text(AppStrings.groups), findsOneWidget);

    await tester.tap(find.text(AppStrings.unread));
    await tester.pumpAndSettle();
    expect(selected, 1);

    await tester.tap(find.text(AppStrings.groups));
    await tester.pumpAndSettle();
    expect(selected, 2);
  });

  testWidgets('LeoTypingIndicator renders status text and dots',
      (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: LeoTypingIndicator(
            text: 'Noah is typing...',
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Noah is typing...'), findsOneWidget);
  });

  testWidgets('LeoVoiceMessagePlayer toggles playback and cycles speed',
      (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(
          body: LeoVoiceMessagePlayer(
            isOwn: false,
            audioUrl: 'https://cdn.leochat.app/voice_0:24.m4a',
            duration: Duration(seconds: 24),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    // Verify initial state
    expect(find.byIcon(Icons.play_arrow_rounded), findsOneWidget);
    expect(find.text('0:24'), findsOneWidget);
    expect(find.text('1x'), findsOneWidget);

    // Tap play button
    await tester.tap(find.byIcon(Icons.play_arrow_rounded));
    await tester.pump();
    expect(find.byIcon(Icons.pause_rounded), findsOneWidget);

    // Tap speed pill to cycle to 1.5x
    await tester.tap(find.text('1x'));
    await tester.pump();
    expect(find.text('1.5x'), findsOneWidget);

    // Tap again to cycle to 2x
    await tester.tap(find.text('1.5x'));
    await tester.pump();
    expect(find.text('2.0x'), findsOneWidget);
  });
}
