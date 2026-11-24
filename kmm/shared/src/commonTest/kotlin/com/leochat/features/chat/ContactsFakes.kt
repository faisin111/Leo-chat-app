package com.leochat.features.chat

import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.users.domain.entities.ProfileUpdate
import com.leochat.features.users.domain.entities.UserProfile
import com.leochat.features.users.domain.entities.UserSearchItem
import com.leochat.features.users.domain.entities.UserSessionItem
import com.leochat.features.users.domain.repositories.UsersRepository

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
}
