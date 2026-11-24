package com.leochat.core.network.stomp

import com.leochat.core.network.config.AppConfig
import com.leochat.core.network.auth.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
                runCatching {
                    httpClient.webSocket(urlString = config.wsBaseUrl) {
                        session = this
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
                                    destinations.value.forEach { (destination, id) -> sendSubscribe(destination, id) }
                                }
                                "MESSAGE" -> this@StompClient.incoming.emit(stomp)
                                "ERROR" -> close()
                            }
                        }
                        heartbeat.cancel()
                    }
                }
                session = null
                mutableState.value = StompConnectionState.Disconnected
                delay(backoff)
                backoff = (backoff * 2).coerceAtMost(MaxReconnectDelayMs)
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        session = null
        mutableState.value = StompConnectionState.Disconnected
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
            created?.let { id -> session?.let { scope.launch { it.sendSubscribe(destination, id) } } }
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
        session?.let { scope.launch { it.sendFrame(StompFrame("UNSUBSCRIBE", mapOf("id" to id))) } }
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

    private suspend fun WebSocketSession.sendSubscribe(destination: String, id: String) {
        sendFrame(StompFrame("SUBSCRIBE", mapOf("id" to id, "destination" to destination, "ack" to "auto")))
    }

    private suspend fun WebSocketSession.sendFrame(frame: StompFrame) {
        send(Frame.Text(StompCodec.encode(frame)))
    }
}
