package com.leochat.feature.settings

import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.auth.domain.model.AuthSession
import com.leochat.feature.auth.domain.model.User
import com.leochat.feature.auth.domain.repository.AuthRepository
import com.leochat.feature.media.domain.model.UploadedAttachment
import com.leochat.feature.media.domain.repository.MediaRepository
import com.leochat.feature.settings.domain.model.BlockedUserItem
import com.leochat.feature.settings.domain.repository.BlockedUsersRepository
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.model.UserPresence
import com.leochat.feature.users.domain.model.UserProfile
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.model.UserSessionItem
import com.leochat.feature.users.domain.repository.UsersRepository
import kotlinx.datetime.Instant

class SettingsFakeUsersRepository(
    var meResult: AppResult<UserProfile> = Either.Right(sampleProfile),
    var updateResult: AppResult<Unit> = Either.Right(Unit),
    var deletePhotoResult: AppResult<Unit> = Either.Right(Unit),
    var sessionsResult: AppResult<List<UserSessionItem>> = Either.Right(sampleSessions),
    var revokeResult: AppResult<Unit> = Either.Right(Unit),
    var deactivateResult: AppResult<Unit> = Either.Right(Unit),
) : UsersRepository {
    val calls = mutableListOf<String>()
    var lastUpdate: ProfileUpdate? = null

    override suspend fun getMe(): AppResult<UserProfile> = meResult.also { calls += "me" }
    override suspend fun getUserById(id: String): AppResult<UserProfile> = meResult
    override suspend fun searchUsers(query: String, page: Int, size: Int): AppResult<List<UserSearchItem>> =
        Either.Right(emptyList())

    override suspend fun updateProfile(update: ProfileUpdate): AppResult<Unit> {
        calls += "update"
        lastUpdate = update
        return updateResult
    }

    override suspend fun deleteProfilePicture(): AppResult<Unit> {
        calls += "deletePhoto"
        return deletePhotoResult
    }

    override suspend fun getSessions(): AppResult<List<UserSessionItem>> = sessionsResult.also { calls += "sessions" }
    override suspend fun revokeSession(sessionId: String): AppResult<Unit> = revokeResult.also { calls += "revoke:$sessionId" }
    override suspend fun deactivateAccount(): AppResult<Unit> = deactivateResult.also { calls += "deactivate" }
    override suspend fun getPresence(ids: List<String>): AppResult<List<UserPresence>> = Either.Right(emptyList())

    companion object {
        val sampleProfile = UserProfile(
            id = "1",
            username = "alex",
            displayName = "Alex Leo",
            email = "alex@leo.chat",
            bio = "Hello there",
            profilePictureUrl = "https://cdn/alex.png",
        )
        val sampleSessions = listOf(
            UserSessionItem(sessionId = "s1", createdAt = Instant.fromEpochSeconds(0), platform = "android"),
            UserSessionItem(sessionId = "s2", createdAt = Instant.fromEpochSeconds(0), deviceName = "MacBook"),
        )
        val apiFailure = Either.Left(AppFailure.Api("Server said no", statusCode = 400))
    }
}

class SettingsFakeAuthRepository(
    var currentUser: User? = User("1", "alex", "alex@leo.chat", "Alex"),
    var changePasswordResult: AppResult<Unit> = Either.Right(Unit),
) : AuthRepository {
    val calls = mutableListOf<String>()

    override suspend fun login(username: String, password: String): AppResult<AuthSession> = error("unused")
    override suspend fun register(username: String, email: String, password: String, displayName: String): AppResult<AuthSession> =
        error("unused")

    override suspend fun logout(): AppResult<Unit> = Either.Right(Unit).also { calls += "logout" }
    override suspend fun logoutAll(): AppResult<Unit> = Either.Right(Unit)
    override suspend fun getCurrentUser(): AppResult<User?> = Either.Right(currentUser)
    override suspend fun hasActiveSession(): Boolean = currentUser != null
    override suspend fun forgotPassword(email: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> =
        changePasswordResult.also { calls += "change:$currentPassword:$newPassword" }

    override suspend fun verifyEmail(token: String): AppResult<Unit> = Either.Right(Unit)
    override suspend fun resendVerification(email: String?): AppResult<Unit> = Either.Right(Unit).also { calls += "resend:$email" }
}

class SettingsFakeMediaRepository(
    var uploadResult: AppResult<UploadedAttachment> = Either.Right(
        UploadedAttachment(
            storageKey = "avatars/uploaded.jpg",
            url = "https://cdn/uploaded.jpg",
            fileName = "avatar.jpg",
            mimeType = "image/jpeg",
            sizeBytes = 1024L,
        ),
    ),
) : MediaRepository {
    val uploadedFiles = mutableListOf<String>()

    override suspend fun uploadVoice(filePath: String): AppResult<String> = error("unused")
    override suspend fun uploadAttachment(uriOrPath: String, fileName: String, mimeType: String): AppResult<UploadedAttachment> {
        uploadedFiles += "$uriOrPath:$fileName:$mimeType"
        return uploadResult
    }

    override suspend fun getDownloadUrl(storageKey: String): AppResult<String> = Either.Right("https://cdn/$storageKey")
}

class SettingsFakeBlockedUsersRepository(
    var result: AppResult<List<BlockedUserItem>> = Either.Right(sampleBlocked),
) : BlockedUsersRepository {
    override suspend fun getBlockedUsers(): AppResult<List<BlockedUserItem>> = result

    companion object {
        val sampleBlocked = listOf(
            BlockedUserItem(userId = "b1", displayName = "Mallory", username = "mallory"),
            BlockedUserItem(userId = "b2", displayName = "Eve", username = "eve"),
        )
    }
}
