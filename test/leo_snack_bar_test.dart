import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/core/widgets/common_widgets/feedback/leo_snack_bar.dart';

void main() {
  testWidgets('LeoSnackBar displays success snackbar correctly',
      (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => LeoSnackBar.showSuccess(
                context,
                'Everything went great!',
              ),
              child: const Text('Show Success'),
            ),
          ),
        ),
      ),
    );

    await tester.tap(find.text('Show Success'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text('Success'), findsOneWidget);
    expect(find.text('Everything went great!'), findsOneWidget);
    expect(find.byIcon(Icons.check_circle_rounded), findsOneWidget);
  });

  testWidgets('LeoSnackBar displays error snackbar correctly', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: Builder(
            builder: (context) => ElevatedButton(
              onPressed: () => LeoSnackBar.showError(
                context,
                'Something failed.',
              ),
              child: const Text('Show Error'),
            ),
          ),
        ),
      ),
    );

    await tester.tap(find.text('Show Error'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.text('Error'), findsOneWidget);
    expect(find.text('Something failed.'), findsOneWidget);
    expect(find.byIcon(Icons.error_outline_rounded), findsOneWidget);
  });
}
