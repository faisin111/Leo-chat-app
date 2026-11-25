package com.leochat.core.network.stomp

import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.network.config.AppConfig
import com.leochat.core.network.auth.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

enum class StompConnectionState { Disconnected, Connecting, Connected }

private const val InitialReconnectDelayMs = 1_000L
private const val MaxReconnectDelayMs = 30_000L
private const val HeartbeatMs = 10_000L

/**
 * STOMP-over-WebSocket transport. Keeps subscriptions across reconnects,
 * reconnects with exponential backoff, and resubscribes after every CONNECTED.
 */
class StompClient(
    private val httpClient: HttpClient,
    private val config: AppConfig,
    private val tokens: TokenStore,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(StompConnectionState.Disconnected)
    val state: StateFlow<StompConnectionState> = mutableState.asStateFlow()

    private val destinations = MutableStateFlow<Map<String, String>>(emptyMap()) // destination -> subscription id
    private val incoming = MutableSharedFlow<StompFrame>(extraBufferCapacity = 128)
    private val subscribeMutex = Mutex()
    private val sentSubscriptionIds = mutableSetOf<String>()
    private var connectionJob: Job? = null
    private var session: WebSocketSession? = null
    private var nextSubscriptionId = 0

    fun connect() {
        if (connectionJob?.isActive == true) return
        connectionJob = scope.launch {
            var backoff = InitialReconnectDelayMs
            while (isActive) {
                val token = tokens.accessToken()
                if (token == null) {
                    mutableState.value = StompConnectionState.Disconnected
                    return@launch
                }
                mutableState.value = StompConnectionState.Connecting
                var forceQuickRetry = false
                runCatching {
                    httpClient.webSocket(urlString = config.wsBaseUrl) {
                        subscribeMutex.withLock {
                            session = this
                            sentSubscriptionIds.clear()
                        }
                        sendFrame(
                            StompFrame(
                                "CONNECT",
                                mapOf(
                                    "accept-version" to "1.2",
                                    "host" to "leo-chat",
                                    "heart-beat" to "$HeartbeatMs,$HeartbeatMs",
                                    "Authorization" to "Bearer $token",
                                ),
                            ),
                        )
                        val heartbeat = launch {
                            while (isActive) {
                                delay(HeartbeatMs)
                                send(Frame.Text("\n"))
                            }
                        }
                        for (frame in incoming) {
                            if (frame !is Frame.Text) continue
                            val stomp = StompCodec.decode(frame.readText()) ?: continue
                            when (stomp.command) {
                                "CONNECTED" -> {
                                    mutableState.value = StompConnectionState.Connected
                                    backoff = InitialReconnectDelayMs
                                    subscribeMutex.withLock {
                                        destinations.value.forEach { (destination, id) ->
                                            if (sentSubscriptionIds.add(id)) sendSubscribe(destination, id)
                                        }
                                    }
                                }
                                "MESSAGE" -> this@StompClient.incoming.emit(stomp)
                                "ERROR" -> {
                                    val msg = (stomp.headers["message"].orEmpty() + " " + stomp.body).lowercase()
                                    val isAuthErr = state.value != StompConnectionState.Connected ||
                                        msg.contains("unauth") || msg.contains("forbidden") ||
                                        msg.contains("token") || msg.contains("expired")
                                    if (isAuthErr && refreshTokenIfNeeded()) {
                                        forceQuickRetry = true
                                    }
                                    close()
                                }
                            }
                        }
                        heartbeat.cancel()
                    }
                }
                subscribeMutex.withLock {
                    session = null
                    sentSubscriptionIds.clear()
                }
                mutableState.value = StompConnectionState.Disconnected
                if (forceQuickRetry) {
                    delay(250L)
                    backoff = InitialReconnectDelayMs
                } else {
                    delay(backoff)
                    backoff = (backoff * 2).coerceAtMost(MaxReconnectDelayMs)
                }
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        session = null
        mutableState.value = StompConnectionState.Disconnected
    }

    /** Ensures STOMP is connected, waiting up to [timeoutMs] when currently connecting/reconnecting. */
    suspend fun awaitConnected(timeoutMs: Long = 4_000L): Boolean {
        if (state.value == StompConnectionState.Connected && session != null) return true
        if (tokens.accessToken() == null) return false
        connect()
        return withTimeoutOrNull(timeoutMs) {
            state.first { it == StompConnectionState.Connected && session != null }
        } != null
    }

    /** Registers [destination] (idempotent) and returns frames delivered to it. */
    fun subscribe(destination: String): Flow<StompFrame> {
        var created: String? = null
        destinations.update { current ->
            if (current.containsKey(destination)) current
            else {
                val id = "sub-${nextSubscriptionId++}"
                created = id
                current + (destination to id)
            }
        }
        // Before CONNECTED, the CONNECTED handler subscribes everything in `destinations`.
        if (state.value == StompConnectionState.Connected) {
            created?.let { id ->
                scope.launch {
                    subscribeMutex.withLock {
                        val active = session
                        if (active != null && state.value == StompConnectionState.Connected && sentSubscriptionIds.add(id)) {
                            runCatching { active.sendSubscribe(destination, id) }
                        }
                    }
                }
            }
        }
        // Spring rewrites `/user/...` destinations, so match on subscription id as well.
        return incoming.filter { frame ->
            val id = destinations.value[destination]
            frame.headers["destination"] == destination || (id != null && frame.headers["subscription"] == id)
        }
    }

    fun unsubscribe(destination: String) {
        val id = destinations.value[destination] ?: return
        destinations.update { it - destination }
        session?.let { active ->
            scope.launch {
                subscribeMutex.withLock {
                    sentSubscriptionIds.remove(id)
                }
                runCatching { active.sendFrame(StompFrame("UNSUBSCRIBE", mapOf("id" to id))) }
            }
        }
    }

    /** Publishes a JSON body. Returns false when there is no live connection. */
    suspend fun send(destination: String, jsonBody: String): Boolean {
        val active = session ?: return false
        if (state.value != StompConnectionState.Connected) return false
        return runCatching {
            active.sendFrame(
                StompFrame(
                    "SEND",
                    mapOf("destination" to destination, "content-type" to "application/json"),
                    jsonBody,
                ),
            )
        }.isSuccess
    }

    private suspend fun refreshTokenIfNeeded(): Boolean {
        val refresh = tokens.refreshToken() ?: return false
        return runCatching {
            val response = httpClient.post(config.apiBaseUrl.trimEnd('/') + "/auth/refresh") {
                setBody(TextContent(buildJsonObject { put("refreshToken", refresh) }.toString(), ContentType.Application.Json))
            }
            if (response.status != HttpStatusCode.OK) return false
            val body = LeoJson.parseToJsonElement(response.bodyAsText()) as? JsonObject ?: return false
            val newAccess = body["accessToken"]?.jsonPrimitive?.content ?: return false
            val newRefresh = body["refreshToken"]?.jsonPrimitive?.content
            tokens.saveTokens(newAccess, newRefresh)
            true
        }.getOrDefault(false)
    }

    private suspend fun WebSocketSession.sendSubscribe(destination: String, id: String) {
        sendFrame(StompFrame("SUBSCRIBE", mapOf("id" to id, "destination" to destination, "ack" to "auto")))
    }

    private suspend fun WebSocketSession.sendFrame(frame: StompFrame) {
        send(Frame.Text(StompCodec.encode(frame)))
    }
}
