package com.leochat.features.users.data.repositories

import com.leochat.core.network.safeApiCall
import com.leochat.features.users.data.datasources.UsersRemoteDataSource
import com.leochat.features.users.domain.entities.ProfileUpdate
import com.leochat.features.users.domain.repositories.UsersRepository

class UsersRepositoryImpl(private val remote: UsersRemoteDataSource) : UsersRepository {
    override suspend fun getMe() = safeApiCall { remote.getMe() }
    override suspend fun getUserById(id: String) = safeApiCall { remote.getUserById(id) }
    override suspend fun searchUsers(query: String, page: Int, size: Int) = safeApiCall { remote.searchUsers(query, page, size) }
    override suspend fun updateProfile(update: ProfileUpdate) = safeApiCall { remote.updateProfile(update) }
    override suspend fun getSessions() = safeApiCall { remote.getSessions() }
    override suspend fun revokeSession(sessionId: String) = safeApiCall { remote.revokeSession(sessionId) }
    override suspend fun deactivateAccount() = safeApiCall { remote.deactivateAccount() }
}
