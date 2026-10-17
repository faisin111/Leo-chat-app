import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

import 'package:leochat/app/app.dart';

void main() {
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);
  testWidgets('LeoChat starts with splash then opens onboarding',
      (tester) async {
    await tester.pumpWidget(const ProviderScope(child: LeoChatApp()));
    await tester.pump();

    expect(find.text('LeoChat'), findsOneWidget);
    await tester.pump(const Duration(milliseconds: 1000));
    await tester.pumpAndSettle();

    expect(find.text('Stay close to your people.'), findsOneWidget);
  });

  testWidgets('landing CTA opens chat shell', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: LeoChatApp()));
    await tester.pump(const Duration(milliseconds: 1000));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Skip'));
    await tester.pumpAndSettle();

    await tester.ensureVisible(find.text('Get started'));
    await tester.tap(find.text('Get started'));
    await tester.pumpAndSettle();

    expect(find.text('Messages'), findsOneWidget);
  });
}
