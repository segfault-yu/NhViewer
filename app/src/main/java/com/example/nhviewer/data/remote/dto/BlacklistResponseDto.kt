package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlacklistResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("count") val count: Int? = null
)
