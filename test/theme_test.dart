import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:leochat/core/theme/colors.dart';
import 'package:leochat/core/theme/fields.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('LeoTheme keeps the current visual tokens', () {
    expect(LeoColors.primary.value, 0xFFE64A19);
    expect(LeoFields.inputDecoration.fillColor, LeoColors.input);
    expect(LeoFields.inputDecoration.border, isA<OutlineInputBorder>());
  });
}
