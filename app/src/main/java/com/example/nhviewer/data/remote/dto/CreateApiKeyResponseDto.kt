package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.ApiKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateApiKeyResponseDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("key") val key: String,
    @SerialName("created_at") val createdAt: String
)

fun CreateApiKeyResponseDto.toDomain() = ApiKey(
    id = id,
    name = name,
    key = key,
    createdAt = createdAt,
    lastUsedAt = null
)
