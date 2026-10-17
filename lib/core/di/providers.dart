import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../cache/shared_preferences_cache_store.dart';
import '../cache/cache_store.dart';
import '../config/app_config.dart';
import '../network/dio_client.dart';

final secureStorageProvider = Provider<FlutterSecureStorage>(
  (_) => const FlutterSecureStorage(),
);

final cacheStoreProvider = FutureProvider<CacheStore>((ref) async {
  final preferences = await SharedPreferences.getInstance();
  return SharedPreferencesCacheStore(preferences);
});

final dioClientProvider = Provider<DioClient>((ref) {
  return DioClient(
    config: AppConfig.production,
    storage: ref.watch(secureStorageProvider),
  );
});
