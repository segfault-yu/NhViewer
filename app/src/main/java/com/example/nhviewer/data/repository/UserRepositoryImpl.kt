package com.example.nhviewer.data.repository

import com.example.nhviewer.data.local.TokenManager
import com.example.nhviewer.data.remote.AuthApi
import com.example.nhviewer.data.remote.dto.*
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.User
import com.example.nhviewer.domain.model.UserSession
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.util.runCatchingCancelable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) : UserRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (tokenManager.hasRefreshToken()) {
            CoroutineScope(Dispatchers.IO).launch {
                loadCurrentUser()
            }
        }
    }

    override suspend fun login(
        username: String,
        password: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<User> = runCatchingCancelable {
        val request = LoginRequest(username, password, powChallenge, powNonce, captchaResponse)
        val tokenResponse = api.login(request)
        tokenManager.saveTokens(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken
        )
        val profile = tokenResponse.user.toDomain()
        _authState.value = AuthState.LoggedIn(profile)
        profile
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<User> = runCatchingCancelable {
        val request = RegisterRequest(username, email, password, powChallenge, powNonce, captchaResponse)
        val tokenResponse = api.register(request)
        tokenManager.saveTokens(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken
        )
        val profile = tokenResponse.user.toDomain()
        _authState.value = AuthState.LoggedIn(profile)
        profile
    }

    override suspend fun logout(): Result<Unit> = runCatchingCancelable {
        try {
            val response = api.logout()
            if (!response.isSuccessful) {
                throw Exception("Logout API failed code: ${response.code()}")
            }
        } finally {
            tokenManager.clearTokens()
            _authState.value = AuthState.LoggedOut
        }
    }

    override suspend fun logoutAll(): Result<Unit> = runCatchingCancelable {
        try {
            val response = api.logoutAll()
            if (!response.isSuccessful) {
                throw Exception("Logout All API failed code: ${response.code()}")
            }
        } finally {
            tokenManager.clearTokens()
            _authState.value = AuthState.LoggedOut
        }
    }

    override suspend fun loadCurrentUser(): Result<User> = runCatchingCancelable {
        val profile = api.getUserProfile().toDomain()
        _authState.value = AuthState.LoggedIn(profile)
        profile
    }

    override suspend fun getSessions(): Result<List<UserSession>> = runCatchingCancelable {
        api.getSessions().map { it.toDomain() }
    }

    override suspend fun revokeSession(sessionId: String): Result<Unit> = runCatchingCancelable {
        val response = api.revokeSession(sessionId)
        if (!response.isSuccessful) {
            throw Exception("Revoke session failed: ${response.code()}")
        }
    }

    override suspend fun requestPasswordReset(
        usernameOrEmail: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<Unit> = runCatchingCancelable {
        val response = api.requestResetPassword(PasswordResetRequest(usernameOrEmail, powChallenge, powNonce, captchaResponse))
        if (!response.isSuccessful) {
            throw Exception("Password reset request failed: ${response.code()}")
        }
    }

    override suspend fun confirmPasswordReset(
        token: String,
        newPassword: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<Unit> = runCatchingCancelable {
        val response = api.confirmResetPassword(
            PasswordResetConfirmRequest(token, newPassword, powChallenge, powNonce, captchaResponse)
        )
        if (!response.isSuccessful) {
            throw Exception("Confirm password reset failed: ${response.code()}")
        }
    }

    override suspend fun getPowChallenge(action: String): Result<PowResponseDto> = runCatchingCancelable {
        api.getPowChallenge(action)
    }

    override suspend fun getCaptchaConfig(): Result<CaptchaResponseDto> = runCatchingCancelable {
        api.getCaptchaConfig()
    }
}
