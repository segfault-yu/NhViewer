package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaptchaResponseDto(
    @SerialName("provider") val provider: String,
    @SerialName("site_key") val siteKey: String
)
