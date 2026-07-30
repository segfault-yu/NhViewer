package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 身份用 is_staff/is_superuser 两个布尔位表示，与登录响应内嵌的 UserInfoDto 是同一套字段
@Serializable
data class UserProfileDto(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String,
    @SerialName("slug") val slug: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("theme") val theme: String = "black",
    @SerialName("is_staff") val isStaff: Boolean = false,
    @SerialName("is_superuser") val isSuperuser: Boolean = false,
    @SerialName("about") val about: String = "",
    @SerialName("favorite_tags") val favoriteTags: String = "",
    @SerialName("email") val email: String? = null
)

fun UserProfileDto.toDomain() = User(
    id = id,
    username = username,
    email = email ?: "",
    registeredAt = 0L,
    role = when {
        isSuperuser -> "superuser"
        isStaff -> "staff"
        else -> "user"
    }
)
