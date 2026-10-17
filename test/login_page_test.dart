import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:leochat/features/auth/presentation/pages/login_page.dart';

void main() {
  testWidgets('login page uses username and password fields', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: LoginPage()));
    await tester.pumpAndSettle();

    expect(find.text('Username'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
    expect(find.text('Your people.\nA little closer.'), findsOneWidget);
  });
}
