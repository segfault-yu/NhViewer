package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.ApiKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("last_used_at") val lastUsedAt: String? = null
)

fun ApiKeyDto.toDomain() = ApiKey(
    id = id,
    name = name,
    key = null,
    createdAt = createdAt,
    lastUsedAt = lastUsedAt
)
