class AppConfig {
  const AppConfig({required this.apiBaseUrl});

  final String apiBaseUrl;

  static const production = AppConfig(
    apiBaseUrl: 'https://leo-chat-backend.onrender.com/api/v1',
  );
}
