package com.leochat.feature.settings.domain.repository

import com.leochat.core.common.result.AppResult
import com.leochat.feature.settings.domain.model.BlockedUserItem

interface BlockedUsersRepository {
    suspend fun getBlockedUsers(): AppResult<List<BlockedUserItem>>
}
