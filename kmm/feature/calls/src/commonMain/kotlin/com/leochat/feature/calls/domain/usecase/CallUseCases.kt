package com.leochat.feature.calls.domain.usecase

import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.repository.CallSignalingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class ObserveCallSignalsUseCase(private val repository: CallSignalingRepository) {
    /** Signals with a call id; malformed frames are dropped. */
    operator fun invoke(): Flow<CallSignal> = repository.incomingSignals().filter { it.callId.isNotBlank() }
}

class SendCallSignalUseCase(private val repository: CallSignalingRepository) {
    suspend operator fun invoke(signal: CallSignal): Boolean = repository.send(signal)
}
