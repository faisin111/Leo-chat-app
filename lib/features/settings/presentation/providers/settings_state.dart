class SettingsState {
  const SettingsState({
    this.displayName = '',
    this.username = '',
    this.email = '',
    this.bio = '',
    this.avatarUrl = '',
    this.messageNotifications = true,
    this.messagePreviews = false,
    this.callNotifications = true,
    this.onlineStatus = true,
    this.profileVisibility = 'Contacts',
    this.blockedCount = 0,
    this.isSaving = false,
  });

  final String displayName;
  final String username;
  final String email;
  final String bio;
  final String avatarUrl;
  final bool messageNotifications;
  final bool messagePreviews;
  final bool callNotifications;
  final bool onlineStatus;
  final String profileVisibility;
  final int blockedCount;
  final bool isSaving;

  SettingsState copyWith({
    String? displayName,
    String? username,
    String? email,
    String? bio,
    String? avatarUrl,
    bool? messageNotifications,
    bool? messagePreviews,
    bool? callNotifications,
    bool? onlineStatus,
    String? profileVisibility,
    int? blockedCount,
    bool? isSaving,
  }) {
    return SettingsState(
      displayName: displayName ?? this.displayName,
      username: username ?? this.username,
      email: email ?? this.email,
      bio: bio ?? this.bio,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      messageNotifications: messageNotifications ?? this.messageNotifications,
      messagePreviews: messagePreviews ?? this.messagePreviews,
      callNotifications: callNotifications ?? this.callNotifications,
      onlineStatus: onlineStatus ?? this.onlineStatus,
      profileVisibility: profileVisibility ?? this.profileVisibility,
      blockedCount: blockedCount ?? this.blockedCount,
      isSaving: isSaving ?? this.isSaving,
    );
  }
}
