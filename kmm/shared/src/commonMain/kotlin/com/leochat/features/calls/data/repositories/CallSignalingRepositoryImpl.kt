package com.leochat.features.calls.data.repositories

import com.leochat.features.calls.data.datasources.CallSignalingDataSource
import com.leochat.features.calls.data.models.CallSignalDto
import com.leochat.features.calls.domain.entities.CallSignal
import com.leochat.features.calls.domain.repositories.CallSignalingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class CallSignalingRepositoryImpl(private val dataSource: CallSignalingDataSource) : CallSignalingRepository {
    override fun incomingSignals(): Flow<CallSignal> = dataSource.subscribe().mapNotNull(CallSignalDto::fromJson)

    override suspend fun send(signal: CallSignal): Boolean = dataSource.send(CallSignalDto.toJson(signal))
}
