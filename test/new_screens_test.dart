import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/features/chat/presentation/pages/group_details_page.dart';
import 'package:leochat/features/chat/presentation/pages/new_conversation_page.dart';
import 'package:leochat/features/chat/presentation/pages/shared_media_page.dart';
import 'package:leochat/features/settings/presentation/pages/edit_profile_page.dart';
import 'package:leochat/features/settings/presentation/pages/settings_page.dart';
import 'package:leochat/features/users/domain/entities/user_profile.dart';
import 'package:leochat/features/users/domain/entities/user_search_item.dart';
import 'package:leochat/features/users/presentation/providers/users_providers.dart';

void main() {
  group('SettingsPage and EditProfilePage', () {
    testWidgets('SettingsPage renders profile card and settings sections',
        (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            currentUserProfileProvider.overrideWith(
              (ref) async => const UserProfile(
                id: 'u-1',
                username: 'alexmorgan',
                displayName: 'Alex Morgan',
                email: 'alex@example.com',
                bio: 'Product designer and builder',
              ),
            ),
          ],
          child: const MaterialApp(
            home: SettingsPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

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
        ProviderScope(
          overrides: [
            currentUserProfileProvider.overrideWith(
              (ref) async => const UserProfile(
                id: 'u-1',
                username: 'alexmorgan',
                displayName: 'Alex Morgan',
                email: 'alex@example.com',
                bio: 'Product designer and builder',
              ),
            ),
          ],
          child: const MaterialApp(
            home: SettingsPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      await tester.tap(find.text('Alex Morgan'));
      await tester.pumpAndSettle();

      expect(find.text('Edit profile'), findsOneWidget);
      expect(find.text('Display name'), findsOneWidget);
      expect(find.text('Change profile photo'), findsOneWidget);
      expect(find.text('Save changes'), findsOneWidget);
    });

    testWidgets('EditProfilePage renders form fields from profile',
        (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            currentUserProfileProvider.overrideWith(
              (ref) async => const UserProfile(
                id: 'u-1',
                username: 'alexmorgan',
                displayName: 'Alex Morgan',
                email: 'alex@example.com',
                bio: 'Product designer and builder',
              ),
            ),
          ],
          child: const MaterialApp(
            home: EditProfilePage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text('Edit profile'), findsOneWidget);
      expect(find.text('Display name'), findsOneWidget);
      expect(find.text('Change profile photo'), findsOneWidget);
      expect(find.text('Save changes'), findsOneWidget);
    });
  });

  group('NewConversationPage', () {
    testWidgets('renders search header and action options', (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: NewConversationPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text('New conversation'), findsOneWidget);
      expect(find.text('Create a group'), findsOneWidget);
      expect(find.text('Start a new conversation'), findsOneWidget);
    });

    testWidgets('displays matching users when search returns results',
        (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: [
            userSearchResultsProvider.overrideWith(
              (ref) async => const [
                UserSearchItem(
                  id: 'u-2',
                  username: 'maya_lin',
                  displayName: 'Maya Lin',
                ),
                UserSearchItem(
                  id: 'u-3',
                  username: 'noah_w',
                  displayName: 'Noah Williams',
                ),
              ],
            ),
          ],
          child: const MaterialApp(
            home: NewConversationPage(),
          ),
        ),
      );

      await tester.pumpAndSettle();

      await tester.enterText(
          find.widgetWithText(TextField, 'Search by name or username'), 'maya');
      await tester.pumpAndSettle();

      expect(find.text('Maya Lin'), findsOneWidget);
      expect(find.text('Noah Williams'), findsOneWidget);
      expect(find.text('@maya_lin'), findsOneWidget);
      expect(find.text('@noah_w'), findsOneWidget);
    });
  });

  group('GroupDetailsPage', () {
    testWidgets('renders group hero, quick actions, and members section',
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

      await tester.pumpAndSettle();

      expect(find.text('Group details'), findsOneWidget);
      expect(find.text('Weekend people'), findsOneWidget);
      expect(find.text('Mute'), findsOneWidget);
      expect(find.text('Media'), findsOneWidget);
      expect(find.text('Shared media & files'), findsOneWidget);
      expect(find.text('Members · 0'), findsOneWidget);
      expect(find.text('No members found'), findsOneWidget);
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

      await tester.pumpAndSettle();

      await tester.tap(find.text('Shared media & files'));
      await tester.pumpAndSettle();

      expect(find.text('Shared in Shared with Weekend people'), findsOneWidget);
      expect(find.text('Everything in one place'), findsOneWidget);
      expect(find.text('Media (0)'), findsOneWidget);
      expect(find.text('Files (0)'), findsOneWidget);
      expect(find.text('Links (0)'), findsOneWidget);
    });
  });

  group('SharedMediaPage', () {
    testWidgets('renders filter pills and empty state when no media shared',
        (tester) async {
      await tester.pumpWidget(
        const ProviderScope(
          child: MaterialApp(
            home: SharedMediaPage(
              conversationTitle: 'Maya',
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text('Shared in Maya'), findsOneWidget);
      expect(find.text('Everything in one place'), findsOneWidget);
      expect(find.text('Media (0)'), findsOneWidget);
      expect(find.text('Files (0)'), findsOneWidget);
      expect(find.text('Links (0)'), findsOneWidget);
      expect(find.text('No media shared yet'), findsOneWidget);
      expect(find.text('Photos and videos sent in this chat will appear here.'),
          findsOneWidget);
    });
  });
}
