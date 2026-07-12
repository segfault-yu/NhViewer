package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.UserSession
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSessionDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("user_agent") val userAgent: String,
    @SerialName("ip_address") val ipAddress: String,
    @SerialName("last_active_at") val lastActiveAt: Long,
    @SerialName("current_session") val currentSession: Boolean
)

fun UserSessionDto.toDomain() = UserSession(
    sessionId = sessionId,
    userAgent = userAgent,
    ipAddress = ipAddress,
    lastActiveAt = lastActiveAt,
    currentSession = currentSession
)
