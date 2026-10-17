import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:fpdart/fpdart.dart';
import 'package:google_fonts/google_fonts.dart';

import 'package:leochat/app/app.dart';
import 'package:leochat/core/di/providers.dart';
import 'package:leochat/core/network/result.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:leochat/features/auth/domain/entities/auth_session.dart';
import 'package:leochat/features/auth/domain/entities/user.dart';
import 'package:leochat/features/auth/domain/repositories/auth_repository.dart';
import 'package:leochat/features/chat/domain/entities/conversation.dart';
import 'package:leochat/features/chat/domain/entities/message.dart';
import 'package:leochat/features/chat/domain/repositories/chat_repository.dart';

class _FakeChatRepository implements ChatRepository {
  @override
  Future<Result<List<Conversation>>> getConversations(
          {int? cursor, int limit = 50, String? query}) async =>
      const Right([]);

  @override
  Future<Result<List<Message>>> getMessages(
          {required String conversationId,
          int? beforeSeq,
          int? afterSeq,
          int limit = 50}) async =>
      const Right([]);

  @override
  Future<Result<Message>> sendMessage(
          {required String conversationId,
          required String clientMessageId,
          required String content,
          String type = 'TEXT',
          String? replyToId}) async =>
      Right(
          Message(id: 'm-1', conversationId: conversationId, content: content));

  @override
  Future<Result<void>> markConversationRead(
          {required String conversationId, required int upToSeq}) async =>
      const Right(null);

  @override
  Future<Result<int>> getUnreadCount() async => const Right(0);

  @override
  Future<Result<Conversation>> getConversation(
          {required String conversationId}) async =>
      Right(Conversation(id: conversationId, title: 'Chat'));
}

class _FakeAuthRepository implements AuthRepository {
  @override
  Future<Result<AuthSession>> login({
    required String username,
    required String password,
  }) async =>
      const Right(AuthSession(
        accessToken: 'test_token',
        refreshToken: 'test_refresh',
        user: User(
          id: '1',
          username: 'alexmorgan',
          email: 'alex@example.com',
          displayName: 'Alex Morgan',
        ),
      ));

  @override
  Future<Result<AuthSession>> register({
    required String username,
    required String email,
    required String password,
    required String displayName,
  }) async =>
      const Right(AuthSession(
        accessToken: 'test_token',
        refreshToken: 'test_refresh',
        user: User(
          id: '1',
          username: 'alexmorgan',
          email: 'alex@example.com',
          displayName: 'Alex Morgan',
        ),
      ));

  @override
  Future<Result<void>> logout() async => const Right(null);

  @override
  Future<Result<User?>> getCurrentUser() async => const Right(null);
}

void main() {
  setUpAll(() {
    GoogleFonts.config.allowRuntimeFetching = false;
  });

  setUp(() {
    FlutterSecureStorage.setMockInitialValues({});
  });

  testWidgets('LeoChat starts with splash then opens onboarding',
      (tester) async {
    await tester.pumpWidget(const ProviderScope(child: LeoChatApp()));
    await tester.pump();

    expect(find.text('LeoChat'), findsOneWidget);
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();

    expect(find.text('Stay close to your people.'), findsOneWidget);
  });

  testWidgets('onboarding skip opens login then chat shell', (tester) async {
    await tester.pumpWidget(ProviderScope(
      overrides: [
        authRepositoryProvider.overrideWithValue(_FakeAuthRepository()),
        chatRepositoryProvider.overrideWithValue(_FakeChatRepository()),
      ],
      child: const LeoChatApp(),
    ));
    await tester.pump(const Duration(seconds: 3));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Skip'));
    await tester.pumpAndSettle();

    expect(find.text('Username'), findsOneWidget);
    await tester.enterText(find.byType(TextField).at(0), 'alexmorgan');
    await tester.enterText(find.byType(TextField).at(1), 'password123');
    await tester.ensureVisible(find.text('Sign in'));
    await tester.tap(find.text('Sign in'));
    await tester.pumpAndSettle();

    expect(find.text('Your chats'), findsOneWidget);
  });
}
