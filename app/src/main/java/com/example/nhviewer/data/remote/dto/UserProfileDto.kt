package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("registered_at") val registeredAt: Long,
    @SerialName("role") val role: String
)

fun UserProfileDto.toDomain() = User(
    id = id,
    username = username,
    email = email,
    registeredAt = registeredAt,
    role = role
)
