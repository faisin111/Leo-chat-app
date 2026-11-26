package com.leochat.feature.users.data.remote

import com.leochat.core.network.bool
import com.leochat.core.network.firstString
import com.leochat.core.network.int
import com.leochat.core.network.string
import com.leochat.core.common.util.parseInstant
import com.leochat.feature.users.domain.model.UserPresence
import com.leochat.feature.users.domain.model.UserProfile
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.model.UserSessionItem
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

object UserDtoMapper {
    fun profile(json: JsonObject) = UserProfile(
        id = json.string("id").orEmpty(),
        username = json.string("username").orEmpty(),
        displayName = json.string("displayName"),
        email = json.string("email"),
        role = json.string("role"),
        profilePictureUrl = json.string("profilePictureUrl"),
        phoneNumber = json.string("phoneNumber"),
        bio = json.string("bio"),
        age = json.int("age"),
        region = json.string("region"),
        isEmailVerified = json.bool("isEmailVerified") == true || json.bool("emailVerified") == true,
    )

    fun searchItem(json: JsonObject) = UserSearchItem(
        id = json.string("id").orEmpty(),
        username = json.string("username").orEmpty(),
        displayName = json.string("displayName"),
        profilePictureUrl = json.string("profilePictureUrl"),
        bio = json.string("bio"),
        region = json.string("region"),
        age = json.int("age"),
        status = json.string("status") ?: "ACTIVE",
        lastSeenAt = parseInstant(json.string("lastSeenAt")),
    )

    fun session(json: JsonObject) = UserSessionItem(
        sessionId = json.string("sessionId") ?: json.string("id").orEmpty(),
        createdAt = parseInstant(json.string("createdAt")) ?: Instant.fromEpochMilliseconds(0),
        lastUsedAt = parseInstant(json.string("lastUsedAt")),
        expiresAt = parseInstant(json.string("expiresAt")),
        logoutAt = parseInstant(json.string("logoutAt")),
        userAgent = json.string("userAgent"),
        ipAddress = json.string("ipAddress"),
        deviceName = json.string("deviceName"),
        platform = json.string("platform"),
        appVersion = json.string("appVersion"),
        active = json.bool("active") != false,
    )

    fun presence(json: JsonObject) = UserPresence(
        userId = json.firstString("userId", "id").orEmpty(),
        online = json.bool("online"),
        status = json.string("status") ?: "ACTIVE",
        lastSeenAt = parseInstant(json.string("lastSeenAt")),
    )
}
