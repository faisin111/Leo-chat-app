package com.leochat.core.common.result

import com.leochat.core.common.constants.AppStrings

/** Failures exposed to the domain and presentation layers. Never leak Ktor exceptions. */
sealed class AppFailure(open val message: String) {
    data class Network(override val message: String = AppStrings.networkError) : AppFailure(message)
    data class Api(
        override val message: String,
        val code: String? = null,
        val statusCode: Int? = null,
    ) : AppFailure(message)
    data object Unauthorized : AppFailure(AppStrings.unauthorized)
    data class Forbidden(override val message: String) : AppFailure(message)
    data class Validation(override val message: String) : AppFailure(message)
    data class Unknown(override val message: String = AppStrings.unexpectedError) : AppFailure(message)
}
