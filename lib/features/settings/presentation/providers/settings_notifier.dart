import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'settings_state.dart';

final settingsNotifierProvider =
    StateNotifierProvider<SettingsNotifier, SettingsState>((ref) {
  return SettingsNotifier();
});

class SettingsNotifier extends StateNotifier<SettingsState> {
  SettingsNotifier() : super(const SettingsState());

  void toggleMessageNotifications(bool val) {
    state = state.copyWith(messageNotifications: val);
  }

  void toggleMessagePreviews(bool val) {
    state = state.copyWith(messagePreviews: val);
  }

  void toggleCallNotifications(bool val) {
    state = state.copyWith(callNotifications: val);
  }

  void toggleOnlineStatus(bool val) {
    state = state.copyWith(onlineStatus: val);
  }

  void updateProfile({
    String? displayName,
    String? username,
    String? email,
    String? bio,
    String? avatarUrl,
  }) {
    state = state.copyWith(
      displayName: displayName ?? state.displayName,
      username: username ?? state.username,
      email: email ?? state.email,
      bio: bio ?? state.bio,
      avatarUrl: avatarUrl ?? state.avatarUrl,
    );
  }
}
