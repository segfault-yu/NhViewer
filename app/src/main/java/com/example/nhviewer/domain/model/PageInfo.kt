package com.example.nhviewer.domain.model

data class PageInfo(
    val number: Int,
    val path: String,
    val width: Int,
    val height: Int,
    val thumbnail: String,
    val thumbnailWidth: Int,
    val thumbnailHeight: Int
)
