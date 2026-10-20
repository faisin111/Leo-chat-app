import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/chat/presentation/pages/group_details_page.dart';
import 'package:leochat/features/chat/presentation/pages/new_conversation_page.dart';
import 'package:leochat/features/chat/presentation/pages/shared_media_page.dart';
import 'package:leochat/features/settings/presentation/pages/edit_profile_page.dart';
import 'package:leochat/features/settings/presentation/pages/settings_page.dart';
import 'package:leochat/features/settings/presentation/providers/settings_notifier.dart';

void main() {
  group('SettingsPage and EditProfilePage', () {
    testWidgets('SettingsPage renders profile card and settings sections',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: SettingsPage(),
          ),
        ),
      );

      expect(find.text('Settings'), findsOneWidget);
      expect(find.text('Alex Morgan'), findsOneWidget);
      expect(find.text('@alexmorgan · Edit profile'), findsOneWidget);
      expect(find.text('NOTIFICATIONS'), findsOneWidget);
      expect(find.text('Message notifications'), findsOneWidget);
      expect(find.text('PRIVACY & ACCOUNT'), findsOneWidget);
      expect(find.text('Profile visibility'), findsOneWidget);
      expect(find.text('Sign out'), findsOneWidget);
    });

    testWidgets('Tapping profile card in SettingsPage opens EditProfilePage',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: SettingsPage(),
          ),
        ),
      );

      await tester.tap(find.text('Alex Morgan'));
      await tester.pumpAndSettle();

      expect(find.text('Edit profile'), findsOneWidget);
      expect(find.text('Display name'), findsOneWidget);
      expect(find.text('Change profile photo'), findsOneWidget);
      expect(find.text('Save changes'), findsOneWidget);
    });

    testWidgets('EditProfilePage updates display name and saves',
        (tester) async {
      final container = ProviderContainer();
      addTearDown(container.dispose);

      await tester.pumpWidget(
        UncontrolledProviderScope(
          container: container,
          child: const MaterialApp(
            home: EditProfilePage(),
          ),
        ),
      );

      expect(find.text('Alex Morgan'), findsOneWidget);

      await tester.enterText(find.byType(TextField).first, 'Alexander M.');
      await tester.tap(find.byIcon(Icons.check_rounded));
      await tester.pumpAndSettle();

      expect(
          container.read(settingsNotifierProvider).displayName, 'Alexander M.');
    });
  });

  group('NewConversationPage', () {
    testWidgets('renders search, favorites and contact rows', (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: NewConversationPage(),
          ),
        ),
      );

      expect(find.text('New conversation'), findsOneWidget);
      expect(find.text('Create a group'), findsOneWidget);
      expect(find.text('Invite a friend'), findsOneWidget);
      expect(find.text('YOUR FAVORITES'), findsOneWidget);
      expect(find.text('ON LEOCHAT'), findsOneWidget);
      expect(find.text('Maya'), findsOneWidget);
      expect(find.text('Ellie Park'), findsOneWidget);
      expect(find.text("Can't find someone?"), findsOneWidget);
    });

    testWidgets('Search filters contact list dynamically', (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: NewConversationPage(),
          ),
        ),
      );

      expect(find.text('Ellie Park'), findsOneWidget);
      expect(find.text('Noah Williams'), findsOneWidget);

      await tester.enterText(
          find.widgetWithText(TextField, 'Search by name or username'),
          'Ellie');
      await tester.pumpAndSettle();

      expect(find.text('Ellie Park'), findsOneWidget);
      expect(find.text('Noah Williams'), findsNothing);
    });
  });

  group('GroupDetailsPage', () {
    testWidgets('renders group hero, quick actions, and members list',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: GroupDetailsPage(
              groupName: 'Weekend people',
            ),
          ),
        ),
      );

      expect(find.text('Group details'), findsOneWidget);
      expect(find.text('Weekend people'), findsOneWidget);
      expect(find.text('Call'), findsOneWidget);
      expect(find.text('Search'), findsOneWidget);
      expect(find.text('Mute'), findsOneWidget);
      expect(find.text('Shared media & files'), findsOneWidget);
      expect(find.text('Group invite link'), findsOneWidget);
      expect(find.text('Group owner'), findsOneWidget);
      expect(find.text('Admin'), findsOneWidget);
      expect(find.text('Leave group'), findsOneWidget);
    });

    testWidgets('Tapping Shared media & files opens SharedMediaPage',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: GroupDetailsPage(
              groupName: 'Weekend people',
            ),
          ),
        ),
      );

      await tester.tap(find.text('Shared media & files'));
      await tester.pumpAndSettle();

      expect(find.text('Weekend people'), findsOneWidget);
      expect(find.text('Everything, all in one place'), findsOneWidget);
      expect(find.text('Media 18'), findsOneWidget);
      expect(find.text('Recent files'), findsOneWidget);
    });
  });

  group('SharedMediaPage', () {
    testWidgets('renders filter pills, photos and recent files',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: SharedMediaPage(
              conversationTitle: 'Shared with Maya',
            ),
          ),
        ),
      );

      expect(find.text('Shared with Maya'), findsOneWidget);
      expect(find.text('Media 18'), findsOneWidget);
      expect(find.text('Files 4'), findsOneWidget);
      expect(find.text('Links 2'), findsOneWidget);
      expect(find.text('October 2026'), findsOneWidget);
      expect(find.text('Recent files'), findsOneWidget);
      expect(find.text('Coast weekend.pdf'), findsOneWidget);
      expect(find.text('Train tickets.pdf'), findsOneWidget);
      expect(find.text('Trip playlist.m4a'), findsOneWidget);
      expect(find.text('Photos and files shared in this conversation.'),
          findsOneWidget);
    });
  });
}
