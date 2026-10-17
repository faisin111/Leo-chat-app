import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:leochat/features/landing/presentation/pages/landing_page.dart';

void main() {
  testWidgets('landing page renders brand hero and actions', (tester) async {
    await tester.pumpWidget(
        const ProviderScope(child: MaterialApp(home: LandingPage())));
    await tester.pumpAndSettle();

    expect(find.text('LeoChat'), findsOneWidget);
    expect(find.text('Conversations that feel closer.'), findsOneWidget);
    expect(find.text('Get started'), findsOneWidget);
  });
}
