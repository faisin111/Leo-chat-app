import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:leochat/app/app.dart';

void main() {
  testWidgets('LeoChat shell renders', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: LeoChatApp()));
    await tester.pumpAndSettle();

    expect(find.text('Messages'), findsOneWidget);
  });
}
