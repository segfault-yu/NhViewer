package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.UserSession
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 实际响应体是 SessionListItem：字段名是 id/created_at/expires_at/current，没有 last_active_at；
// 没有真正的"最后活跃时间"，用 created_at（会话创建时间）近似展示
@Serializable
data class UserSessionDto(
    @SerialName("id") val id: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("expires_at") val expiresAt: Long,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("user_agent") val userAgent: String? = null,
    @SerialName("current") val current: Boolean = false
)

fun UserSessionDto.toDomain() = UserSession(
    sessionId = id,
    userAgent = userAgent ?: "",
    ipAddress = ipAddress ?: "",
    lastActiveAt = createdAt,
    currentSession = current
)
