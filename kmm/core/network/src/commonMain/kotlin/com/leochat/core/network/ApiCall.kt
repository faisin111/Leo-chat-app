package com.leochat.core.network

import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Runs a network call and maps every transport/HTTP error to [AppFailure].
 * Data sources throw; repositories wrap calls in this.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    Either.Right(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Either.Left(e.toAppFailure())
}

suspend fun Throwable.toAppFailure(): AppFailure = when (this) {
    is ResponseException -> mapResponse(this)
    is HttpRequestTimeoutException, is ConnectTimeoutException, is IOException -> AppFailure.Network()
    is SerializationException -> AppFailure.Unknown(AppStrings.unexpectedError)
    else -> AppFailure.Unknown(message ?: AppStrings.unexpectedError)
}

private suspend fun mapResponse(error: ResponseException): AppFailure {
    val status = error.response.status
    val body = runCatching { LeoJson.parseToJsonElement(error.response.bodyAsText()) as? JsonObject }.getOrNull()
    val message = body?.get("message")?.jsonPrimitive?.content
        ?: body?.get("error")?.jsonPrimitive?.content
        ?: AppStrings.requestFailed
    val code = body?.get("code")?.jsonPrimitive?.content
    return when (status) {
        HttpStatusCode.Unauthorized -> AppFailure.Unauthorized
        HttpStatusCode.Forbidden -> AppFailure.Forbidden(message)
        else -> AppFailure.Api(message = message, code = code, statusCode = status.value)
    }
}
