import 'package:flutter/foundation.dart';

class AuthSessionEvents {
  AuthSessionEvents._();

  static final ValueNotifier<int> sessionExpired = ValueNotifier<int>(0);

  static void notifyExpired() {
    sessionExpired.value++;
  }
}
