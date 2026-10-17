import 'package:fpdart/fpdart.dart';

import '../errors/app_failure.dart';

typedef Result<T> = Either<AppFailure, T>;
