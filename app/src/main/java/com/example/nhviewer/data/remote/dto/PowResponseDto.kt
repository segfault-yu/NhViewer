package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PowResponseDto(
    @SerialName("challenge") val challenge: String,
    @SerialName("difficulty") val difficulty: Int
)
