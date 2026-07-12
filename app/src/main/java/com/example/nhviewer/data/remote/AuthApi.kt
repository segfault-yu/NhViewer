package com.example.nhviewer.data.remote

import com.example.nhviewer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/v2/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/v2/auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @POST("api/v2/auth/refresh")
    suspend fun refresh(@Body request: TokenRefreshRequest): TokenResponse

    @POST("api/v2/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/v2/auth/logout/all")
    suspend fun logoutAll(): Response<Unit>

    @GET("api/v2/auth/sessions")
    suspend fun getSessions(): List<UserSessionDto>

    @DELETE("api/v2/auth/sessions/{session_id}")
    suspend fun revokeSession(@Path("session_id") sessionId: String): Response<Unit>

    @POST("api/v2/auth/reset")
    suspend fun requestResetPassword(@Body request: PasswordResetRequest): Response<Unit>

    @POST("api/v2/auth/reset/confirm")
    suspend fun confirmResetPassword(@Body request: PasswordResetConfirmRequest): Response<Unit>

    @GET("api/v2/pow")
    suspend fun getPowChallenge(@Query("action") action: String): PowResponseDto

    @GET("api/v2/captcha")
    suspend fun getCaptchaConfig(): CaptchaResponseDto

    @GET("api/v2/user")
    suspend fun getUserProfile(): UserProfileDto
}
