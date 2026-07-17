package com.example.nhviewer.domain.model

data class ApiKey(
    val id: String,
    val name: String?,
    val key: String?,
    val createdAt: String,
    val lastUsedAt: String?
)
