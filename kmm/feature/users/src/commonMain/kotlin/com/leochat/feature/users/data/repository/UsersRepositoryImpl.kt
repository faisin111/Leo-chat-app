package com.leochat.feature.users.data.repository

import com.leochat.core.network.safeApiCall
import com.leochat.feature.users.data.remote.UsersRemoteDataSource
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.repository.UsersRepository

class UsersRepositoryImpl(private val remote: UsersRemoteDataSource) : UsersRepository {
    override suspend fun getMe() = safeApiCall { remote.getMe() }
    override suspend fun getUserById(id: String) = safeApiCall { remote.getUserById(id) }
    override suspend fun searchUsers(query: String, page: Int, size: Int) = safeApiCall { remote.searchUsers(query, page, size) }
    override suspend fun updateProfile(update: ProfileUpdate) = safeApiCall { remote.updateProfile(update) }
    override suspend fun deleteProfilePicture() = safeApiCall { remote.deleteProfilePicture() }
    override suspend fun getSessions() = safeApiCall { remote.getSessions() }
    override suspend fun revokeSession(sessionId: String) = safeApiCall { remote.revokeSession(sessionId) }
    override suspend fun deactivateAccount() = safeApiCall { remote.deactivateAccount() }
    override suspend fun getPresence(ids: List<String>) = safeApiCall { remote.getPresence(ids) }
}
