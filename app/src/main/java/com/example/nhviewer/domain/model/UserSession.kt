package com.example.nhviewer.domain.model

data class UserSession(
    val sessionId: String,
    val userAgent: String,
    val ipAddress: String,
    val lastActiveAt: Long,
    val currentSession: Boolean
)
