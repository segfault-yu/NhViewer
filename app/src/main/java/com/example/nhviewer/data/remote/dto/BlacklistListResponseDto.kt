package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlacklistListResponseDto(
    @SerialName("tags") val tags: List<TagResponseDto>,
    @SerialName("count") val count: Int? = null
)
