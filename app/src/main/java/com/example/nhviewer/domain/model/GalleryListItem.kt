package com.example.nhviewer.domain.model

data class GalleryListItem(
    val id: Int,
    val mediaId: String,
    val englishTitle: String,
    val japaneseTitle: String?,
    val thumbnail: String,
    val thumbnailWidth: Int,
    val thumbnailHeight: Int,
    val numPages: Int,
    val numFavorites: Int,
    val tagIds: List<Int>,
    val blacklisted: Boolean,
    val uploadDate: Long? = null
)
