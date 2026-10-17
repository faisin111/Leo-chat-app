class RegisterRequestDto {
  const RegisterRequestDto({
    required this.username,
    required this.email,
    required this.password,
    required this.displayName,
  });

  final String username;
  final String email;
  final String password;
  final String displayName;

  Map<String, dynamic> toJson() => {
        'username': username,
        'email': email,
        'password': password,
        'displayName': displayName,
      };
}
