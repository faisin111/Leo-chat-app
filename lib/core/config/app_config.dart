class AppConfig {
  const AppConfig({
    required this.apiBaseUrl,
    this.wsBaseUrl = 'wss://leo-chat-backend.onrender.com/ws/websocket',
  });

  final String apiBaseUrl;
  final String wsBaseUrl;

  static const production = AppConfig(
    apiBaseUrl: 'https://leo-chat-backend.onrender.com/api/v1',
    wsBaseUrl: 'wss://leo-chat-backend.onrender.com/ws/websocket',
  );
}
