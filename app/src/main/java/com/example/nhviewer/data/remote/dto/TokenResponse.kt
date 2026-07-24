package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GET /api/v2/auth/login 和 /register 成功响应体
 * 实际 API 返回 access_token、refresh_token 和内嵌的 user 对象，不含 expires_in/token_type
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("user") val user: UserInfoDto
)

@Serializable
data class UserInfoDto(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String,
    @SerialName("slug") val slug: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("theme") val theme: String = "black",
    @SerialName("is_staff") val isStaff: Boolean = false,
    @SerialName("is_superuser") val isSuperuser: Boolean = false
)

fun UserInfoDto.toDomain() = User(
    id = id,
    username = username,
    email = "",
    registeredAt = 0L,
    role = when {
        isSuperuser -> "superuser"
        isStaff -> "staff"
        else -> "user"
    }
)
