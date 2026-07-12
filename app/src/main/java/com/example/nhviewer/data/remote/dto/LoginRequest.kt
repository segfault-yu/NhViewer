package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * POST /api/v2/auth/login 请求体
 * 字段名严格对应 API v2 规格
 */
@Serializable
data class LoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
    @SerialName("pow_challenge") val powChallenge: String,
    @SerialName("pow_nonce") val powNonce: String,
    @SerialName("captcha_response") val captchaResponse: String
)
