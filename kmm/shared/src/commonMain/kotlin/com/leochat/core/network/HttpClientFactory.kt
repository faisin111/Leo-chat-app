package com.leochat.core.network

import com.leochat.core.config.AppConfig
import com.leochat.core.session.SessionEvents
import com.leochat.core.session.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.takeFrom
import io.ktor.http.encodedPath
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private val unauthenticatedPaths = listOf(
    "/auth/login",
    "/auth/register",
    "/auth/logout",
    "/auth/refresh",
    "/auth/forgot-password",
    "/auth/reset-password",
)

/**
 * Builds the shared Ktor client: JSON, timeouts, Bearer token injection,
 * one-shot token refresh on 401, and session expiry broadcast.
 */
fun createHttpClient(
    engine: HttpClientEngine,
    config: AppConfig,
    tokens: TokenStore,
    sessionEvents: SessionEvents,
): HttpClient {
    val client = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(LeoJson) }
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 20_000
        }
        install(WebSockets)
        defaultRequest {
            url(config.apiBaseUrl.trimEnd('/') + "/")
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }
    }

    val refreshMutex = Mutex()

    client.plugin(HttpSend).intercept { request ->
        // Presigned media uploads go to storage hosts and must not carry the Bearer token.
        val isApiHost = request.url.host.isApiHost(config)
        val isAuthPath = unauthenticatedPaths.any { request.url.encodedPath.contains(it) }
        val usedToken = if (isApiHost) tokens.accessToken() else null
        if (usedToken != null && !request.headers.contains(HttpHeaders.Authorization)) {
            request.headers.append(HttpHeaders.Authorization, "Bearer $usedToken")
        }
        val call = execute(request)
        if (call.response.status != HttpStatusCode.Unauthorized || isAuthPath || !isApiHost) {
            return@intercept call
        }

        val freshToken = refreshMutex.withLock {
            val current = tokens.accessToken()
            if (current != null && current != usedToken) return@withLock current
            val refreshToken = tokens.refreshToken() ?: return@withLock null
            runCatching {
                val refreshCall = execute(
                    HttpRequestBuilder().apply {
                        method = HttpMethod.Post
                        url.takeFrom(config.apiBaseUrl.trimEnd('/') + "/auth/refresh")
                        setBodyText(buildJsonObject { put("refreshToken", refreshToken) }.toString())
                    },
                )
                if (refreshCall.response.status != HttpStatusCode.OK) return@runCatching null
                val body = LeoJson.parseToJsonElement(refreshCall.response.bodyAsText()) as? JsonObject
                val newAccess = body?.get("accessToken")?.jsonPrimitive?.content
                val newRefresh = body?.get("refreshToken")?.jsonPrimitive?.content
                if (newAccess.isNullOrBlank()) null else newAccess.also { tokens.saveTokens(it, newRefresh) }
            }.getOrNull()
        }

        if (freshToken != null) {
            request.headers.remove(HttpHeaders.Authorization)
            request.headers.append(HttpHeaders.Authorization, "Bearer $freshToken")
            return@intercept execute(request)
        }

        tokens.clearSession()
        sessionEvents.notifyExpired()
        call
    }

    return client
}

private fun HttpRequestBuilder.setBodyText(json: String) {
    setBody(TextContent(json, ContentType.Application.Json))
}

private fun String.isApiHost(config: AppConfig): Boolean = isNotEmpty() && config.apiBaseUrl.contains("://$this")
