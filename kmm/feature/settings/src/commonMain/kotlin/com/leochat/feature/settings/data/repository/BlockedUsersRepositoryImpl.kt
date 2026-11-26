package com.leochat.feature.settings.data.repository

import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.common.result.AppResult
import com.leochat.core.network.safeApiCall
import com.leochat.feature.settings.data.remote.BlockedUsersRemoteDataSource
import com.leochat.feature.settings.domain.model.BlockedUserItem
import com.leochat.feature.settings.domain.repository.BlockedUsersRepository
import com.leochat.feature.users.domain.repository.UsersRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class BlockedUsersRepositoryImpl(
    private val remote: BlockedUsersRemoteDataSource,
    private val usersRepository: UsersRepository,
) : BlockedUsersRepository {

    override suspend fun getBlockedUsers(): AppResult<List<BlockedUserItem>> = safeApiCall {
        val entries = remote.getBlockedUsers()
        if (entries.isEmpty()) return@safeApiCall emptyList()

        coroutineScope {
            entries.map { entry ->
                async {
                    val profile = usersRepository.getUserById(entry.userId).getOrNull()
                    BlockedUserItem(
                        userId = entry.userId,
                        displayName = profile?.effectiveName ?: SettingsStrings.unknownUser,
                        username = profile?.username,
                        avatarUrl = profile?.profilePictureUrl,
                        blockedAt = entry.blockedAt,
                    )
                }
            }.awaitAll()
        }.sortedByDescending { it.blockedAt }
    }
}
