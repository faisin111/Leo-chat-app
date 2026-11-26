package com.leochat.feature.chat

import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.model.UserPresence
import com.leochat.feature.users.domain.model.UserProfile
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.model.UserSessionItem
import com.leochat.feature.users.domain.repository.UsersRepository

val maya = UserSearchItem("u2", "maya", "Maya Chen")
val sam = UserSearchItem("u3", "sam")

/** Users repository for contacts tests: only search is meaningful. */
class ContactsFakeUsersRepository : UsersRepository {
    var searchResult: AppResult<List<UserSearchItem>> = Either.Right(listOf(maya, sam))
    val queries = mutableListOf<String>()

    override suspend fun searchUsers(query: String, page: Int, size: Int): AppResult<List<UserSearchItem>> {
        queries += query
        return searchResult
    }

    override suspend fun getMe(): AppResult<UserProfile> = Either.Right(UserProfile("u1", "alex"))
    override suspend fun getUserById(id: String): AppResult<UserProfile> = Either.Right(UserProfile(id, id))
    override suspend fun updateProfile(update: ProfileUpdate): AppResult<Unit> = Either.Right(Unit)
    override suspend fun getSessions(): AppResult<List<UserSessionItem>> = Either.Right(emptyList())
    override suspend fun revokeSession(sessionId: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun deactivateAccount(): AppResult<Unit> = Either.Right(Unit)
    override suspend fun getPresence(ids: List<String>): AppResult<List<UserPresence>> = Either.Right(emptyList())
}
