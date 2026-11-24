package com.leochat.core.network.config

/** Backend endpoints. Mirrors `lib/core/config/app_config.dart`. */
data class AppConfig(
    val apiBaseUrl: String,
    val wsBaseUrl: String,
) {
    companion object {
        val Production = AppConfig(
            apiBaseUrl = "https://leo-chat-backend.onrender.com/api/v1",
            wsBaseUrl = "wss://leo-chat-backend.onrender.com/ws/websocket",
        )
    }
}
