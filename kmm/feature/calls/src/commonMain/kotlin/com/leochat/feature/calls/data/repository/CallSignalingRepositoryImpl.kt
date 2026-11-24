package com.leochat.feature.calls.data.repository

import com.leochat.feature.calls.data.remote.CallSignalingDataSource
import com.leochat.feature.calls.data.remote.CallSignalDto
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.repository.CallSignalingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class CallSignalingRepositoryImpl(private val dataSource: CallSignalingDataSource) : CallSignalingRepository {
    override fun incomingSignals(): Flow<CallSignal> = dataSource.subscribe().mapNotNull(CallSignalDto::fromJson)

    override suspend fun send(signal: CallSignal): Boolean = dataSource.send(CallSignalDto.toJson(signal))
}
