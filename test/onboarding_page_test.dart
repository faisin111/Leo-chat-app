import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:google_fonts/google_fonts.dart';

import 'package:leochat/features/onboarding/presentation/pages/onboarding_page.dart';

void main() {
  setUpAll(() => GoogleFonts.config.allowRuntimeFetching = false);
  testWidgets('onboarding uses horizontal pages', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: OnboardingPage()));
    await tester.pumpAndSettle();

    expect(find.text('Stay close to your people.'), findsOneWidget);
    expect(find.text('Next'), findsOneWidget);

    await tester.tap(find.text('Next'));
    await tester.pumpAndSettle();
    expect(find.text('Everything in sync.'), findsOneWidget);
  });
}
