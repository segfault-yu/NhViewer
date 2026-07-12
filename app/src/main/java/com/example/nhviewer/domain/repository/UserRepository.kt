package com.example.nhviewer.domain.repository

import com.example.nhviewer.data.remote.dto.CaptchaResponseDto
import com.example.nhviewer.data.remote.dto.PowResponseDto
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.User
import com.example.nhviewer.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val authState: StateFlow<AuthState>

    suspend fun login(
        username: String,
        password: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<User>

    suspend fun register(
        username: String,
        email: String,
        password: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun logoutAll(): Result<Unit>

    suspend fun loadCurrentUser(): Result<User>

    suspend fun getSessions(): Result<List<UserSession>>

    suspend fun revokeSession(sessionId: String): Result<Unit>

    suspend fun requestPasswordReset(
        usernameOrEmail: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<Unit>

    suspend fun confirmPasswordReset(
        token: String,
        newPassword: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<Unit>

    suspend fun getPowChallenge(action: String): Result<PowResponseDto>

    suspend fun getCaptchaConfig(): Result<CaptchaResponseDto>
}
