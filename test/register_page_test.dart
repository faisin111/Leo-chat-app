import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:leochat/features/auth/presentation/pages/register_page.dart';

void main() {
  testWidgets('register page renders account fields', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: RegisterPage()));
    await tester.pumpAndSettle();

    expect(find.text('Make yourself\nat home.'), findsOneWidget);
    expect(find.text('Display name'), findsOneWidget);
    expect(find.text('Username'), findsOneWidget);
    expect(find.text('Email address'), findsOneWidget);
    expect(find.text('Create password'), findsOneWidget);
    expect(
        find.text(
            'I agree to the Terms of Service and Privacy Policy. Let’s keep this a kind place.'),
        findsOneWidget);
  });
}
