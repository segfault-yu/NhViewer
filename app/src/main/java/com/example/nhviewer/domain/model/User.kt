package com.example.nhviewer.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val registeredAt: Long,
    val role: String
)
