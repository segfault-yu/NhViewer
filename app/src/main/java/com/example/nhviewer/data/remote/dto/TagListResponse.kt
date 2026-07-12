package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagListResponse(
    @SerialName("result") val result: List<TagResponseDto>,
    @SerialName("num_pages") val numPages: Int,
    @SerialName("per_page") val perPage: Int = 25,
    @SerialName("total") val total: Int? = null
)
