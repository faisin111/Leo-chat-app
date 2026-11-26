package com.leochat.feature.settings.domain.usecase

import com.leochat.core.common.result.AppResult
import com.leochat.feature.settings.domain.model.BlockedUserItem
import com.leochat.feature.settings.domain.repository.BlockedUsersRepository

class GetBlockedUsersUseCase(private val repository: BlockedUsersRepository) {
    suspend operator fun invoke(): AppResult<List<BlockedUserItem>> = repository.getBlockedUsers()
}
