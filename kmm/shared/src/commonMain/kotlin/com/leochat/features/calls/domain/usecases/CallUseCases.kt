package com.leochat.features.calls.domain.usecases

import com.leochat.features.calls.domain.entities.CallSignal
import com.leochat.features.calls.domain.repositories.CallSignalingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class ObserveCallSignalsUseCase(private val repository: CallSignalingRepository) {
    /** Signals with a call id; malformed frames are dropped. */
    operator fun invoke(): Flow<CallSignal> = repository.incomingSignals().filter { it.callId.isNotBlank() }
}

class SendCallSignalUseCase(private val repository: CallSignalingRepository) {
    suspend operator fun invoke(signal: CallSignal): Boolean = repository.send(signal)
}
