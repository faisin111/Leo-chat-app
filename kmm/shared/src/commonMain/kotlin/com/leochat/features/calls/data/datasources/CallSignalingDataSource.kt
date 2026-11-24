package com.leochat.features.calls.data.datasources

import com.leochat.core.network.stomp.StompClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val CallsQueue = "/user/queue/calls"
const val CallSignalDestination = "/app/call.signal"

/** Raw STOMP transport for call signals (JSON bodies). */
interface CallSignalingDataSource {
    fun subscribe(): Flow<String>
    suspend fun send(jsonBody: String): Boolean
}

class StompCallSignalingDataSource(private val stomp: StompClient) : CallSignalingDataSource {
    override fun subscribe(): Flow<String> = stomp.subscribe(CallsQueue).map { it.body }

    override suspend fun send(jsonBody: String): Boolean = stomp.send(CallSignalDestination, jsonBody)
}
