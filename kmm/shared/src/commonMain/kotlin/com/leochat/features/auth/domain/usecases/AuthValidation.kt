package com.leochat.features.auth.domain.usecases

/** Form rules shared by auth controllers. Mirrors the checks in the Flutter pages. */
object AuthValidation {
    const val MinPasswordLength = 8

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.contains('@') && trimmed.contains('.')
    }

    fun isStrongEnough(password: String): Boolean = password.length >= MinPasswordLength
}
