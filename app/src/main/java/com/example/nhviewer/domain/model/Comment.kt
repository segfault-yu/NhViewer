package com.example.nhviewer.domain.model

data class Comment(
    val id: Int,
    val galleryId: Int,
    val username: String,
    val avatarUrl: String?,
    val userId: Int,
    val postDate: Long,
    val body: String
)
