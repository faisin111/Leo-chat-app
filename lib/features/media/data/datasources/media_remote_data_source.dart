import 'dart:io';

import 'package:dio/dio.dart';

class MediaRemoteDataSource {
  const MediaRemoteDataSource(this._dio);

  final Dio _dio;

  Future<String> uploadVoice({required String filePath}) async {
    if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
      return filePath;
    }

    final file = File(filePath);
    if (!await file.exists()) {
      return filePath;
    }

    final size = await file.length();
    final isWav = filePath.toLowerCase().endsWith('.wav');
    final contentType = isWav ? 'audio/wav' : 'audio/m4a';

    final presign = await _dio.post<Map<String, dynamic>>(
      '/media/presign',
      data: {'mimeType': contentType, 'sizeInBytes': size},
    );
    final data = presign.data ?? const <String, dynamic>{};
    final uploadUrl = data['uploadUrl']?.toString();
    final storageKey = data['storageKey']?.toString();
    if (uploadUrl == null || storageKey == null) {
      throw const FormatException('Media presign response is incomplete.');
    }

    await Dio().put<void>(
      uploadUrl,
      data: file.openRead(),
      options: Options(
        headers: {
          Headers.contentTypeHeader:
              data['contentType']?.toString() ?? contentType,
          Headers.contentLengthHeader: size,
        },
      ),
    );

    final confirmed = await _dio.post<Map<String, dynamic>>(
      '/media/confirm',
      data: {'storageKey': storageKey},
    );
    final url = confirmed.data?['url']?.toString();
    if (url == null || url.isEmpty) {
      throw const FormatException('Media confirm response has no URL.');
    }
    return url;
  }
}
