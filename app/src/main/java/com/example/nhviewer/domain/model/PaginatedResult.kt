package com.example.nhviewer.domain.model

data class PaginatedResult<T>(
    val items: List<T>,
    val numPages: Int,
    val total: Int? = null
)
