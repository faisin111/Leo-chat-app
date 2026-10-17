import '../../../../core/network/result.dart';

abstract interface class MediaRepository {
  Future<Result<String>> uploadVoice({required String filePath});
}
