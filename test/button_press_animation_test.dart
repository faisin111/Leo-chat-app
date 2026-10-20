import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/core/widgets/common_widgets/buttons/leo_outline_button.dart';
import 'package:leochat/core/widgets/common_widgets/buttons/leo_pressable.dart';
import 'package:leochat/core/widgets/common_widgets/buttons/leo_primary_button.dart';

void main() {
  group('LeoPressable', () {
    testWidgets('renders child at full scale initially', (tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: LeoPressable(
              child: Text('Tap Me'),
            ),
          ),
        ),
      );

      final animatedScale =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScale.scale, 1.0);
    });

    testWidgets('scales down on pointer down and restores on pointer up',
        (tester) async {
      int tapCount = 0;
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: LeoPressable(
              onPressed: () => tapCount++,
              child: const SizedBox(
                width: 100,
                height: 50,
                child: Text('Click'),
              ),
            ),
          ),
        ),
      );

      final gesture =
          await tester.startGesture(tester.getCenter(find.text('Click')));
      await tester.pump(const Duration(milliseconds: 50));

      // After press down, scale is target pressedScale (0.94)
      final animatedScalePressed =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScalePressed.scale, 0.94);

      // Release pointer
      await gesture.up();
      await tester.pumpAndSettle();

      final animatedScaleReleased =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScaleReleased.scale, 1.0);
      expect(tapCount, 1);
    });

    testWidgets('does not scale when disabled', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: LeoPressable(
              enabled: false,
              onPressed: () {},
              child: const SizedBox(
                width: 100,
                height: 50,
                child: Text('Disabled'),
              ),
            ),
          ),
        ),
      );

      final gesture =
          await tester.startGesture(tester.getCenter(find.text('Disabled')));
      await tester.pump(const Duration(milliseconds: 50));

      final animatedScale =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScale.scale, 1.0);

      await gesture.up();
      await tester.pumpAndSettle();
    });
  });

  group('LeoPrimaryButton with press animation', () {
    testWidgets('animates and triggers onPressed callback', (tester) async {
      int clicked = 0;
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: LeoPrimaryButton(
              label: 'Submit',
              onPressed: () => clicked++,
            ),
          ),
        ),
      );

      expect(find.text('Submit'), findsOneWidget);

      final gesture =
          await tester.startGesture(tester.getCenter(find.text('Submit')));
      await tester.pump(const Duration(milliseconds: 60));

      final animatedScale =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScale.scale, 0.94);

      await gesture.up();
      await tester.pumpAndSettle();

      final releasedScale =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(releasedScale.scale, 1.0);
      expect(clicked, 1);
    });

    testWidgets('disabled when isLoading', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: LeoPrimaryButton(
              label: 'Submit',
              isLoading: true,
              onPressed: () {},
            ),
          ),
        ),
      );

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
      final animatedScale =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScale.scale, 1.0);
    });
  });

  group('LeoOutlineButton with press animation', () {
    testWidgets('animates and triggers onPressed callback', (tester) async {
      int clicked = 0;
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: LeoOutlineButton(
              label: 'Cancel',
              onPressed: () => clicked++,
            ),
          ),
        ),
      );

      expect(find.text('Cancel'), findsOneWidget);

      final gesture =
          await tester.startGesture(tester.getCenter(find.text('Cancel')));
      await tester.pump(const Duration(milliseconds: 60));

      final animatedScale =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScale.scale, 0.94);

      await gesture.up();
      await tester.pumpAndSettle();

      final releasedScale =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(releasedScale.scale, 1.0);
      expect(clicked, 1);
    });
  });
}
