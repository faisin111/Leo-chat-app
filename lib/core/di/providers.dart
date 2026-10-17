import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../cache/shared_preferences_cache_store.dart';
import '../cache/cache_store.dart';
import '../config/app_config.dart';
import '../network/dio_client.dart';
import '../network/stomp_service.dart';
import '../../features/auth/data/datasources/auth_remote_data_source.dart';
import '../../features/auth/data/repositories/auth_repository_impl.dart';
import '../../features/auth/domain/repositories/auth_repository.dart';
import '../../features/chat/data/datasources/chat_remote_data_source.dart';
import '../../features/chat/data/repositories/chat_repository_impl.dart';
import '../../features/chat/domain/repositories/chat_repository.dart';
import '../../features/media/data/datasources/media_remote_data_source.dart';
import '../../features/media/data/repositories/media_repository_impl.dart';
import '../../features/media/domain/repositories/media_repository.dart';

final appConfigProvider = Provider<AppConfig>((_) => AppConfig.production);

final secureStorageProvider = Provider<FlutterSecureStorage>(
  (_) => const FlutterSecureStorage(),
);

final cacheStoreProvider = FutureProvider<CacheStore>((ref) async {
  final preferences = await SharedPreferences.getInstance();
  return SharedPreferencesCacheStore(preferences);
});

final dioClientProvider = Provider<DioClient>((ref) {
  return DioClient(
    config: ref.watch(appConfigProvider),
    storage: ref.watch(secureStorageProvider),
  );
});

final stompServiceProvider = Provider<StompService>((ref) {
  final config = ref.watch(appConfigProvider);
  final service = StompService(wsUrl: config.wsBaseUrl);
  ref.onDispose(service.dispose);
  return service;
});

final authRemoteDataSourceProvider = Provider<AuthRemoteDataSource>((ref) {
  final dio = ref.watch(dioClientProvider).dio;
  return AuthRemoteDataSourceImpl(dio);
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepositoryImpl(
    remoteDataSource: ref.watch(authRemoteDataSourceProvider),
    secureStorage: ref.watch(secureStorageProvider),
  );
});

final chatRemoteDataSourceProvider = Provider<ChatRemoteDataSource>((ref) {
  final dio = ref.watch(dioClientProvider).dio;
  return ChatRemoteDataSourceImpl(dio);
});

final chatRepositoryProvider = Provider<ChatRepository>((ref) {
  return ChatRepositoryImpl(
    remoteDataSource: ref.watch(chatRemoteDataSourceProvider),
    secureStorage: ref.watch(secureStorageProvider),
    cacheStore: ref.watch(cacheStoreProvider).valueOrNull,
  );
});

final mediaRepositoryProvider = Provider<MediaRepository>((ref) {
  return MediaRepositoryImpl(
    MediaRemoteDataSource(ref.watch(dioClientProvider).dio),
  );
});
