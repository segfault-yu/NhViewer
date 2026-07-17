package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateApiKeyRequest(
    @SerialName("name") val name: String,
    @SerialName("pow_challenge") val powChallenge: String,
    @SerialName("pow_nonce") val powNonce: String,
    @SerialName("captcha_response") val captchaResponse: String
)
