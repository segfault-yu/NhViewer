package com.example.nhviewer.domain.model

data class ReadingHistory(
    val galleryId: Int,
    val mediaId: String,
    val title: String,
    val lastReadPage: Int,
    val totalPages: Int,
    val timestamp: Long
)
