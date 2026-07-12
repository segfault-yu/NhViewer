package com.example.nhviewer.domain.model

data class GalleryDetail(
    val id: Int,
    val mediaId: String,
    val englishTitle: String,
    val japaneseTitle: String?,
    val prettyTitle: String?,
    val coverPath: String,
    val coverWidth: Int,
    val coverHeight: Int,
    val thumbnailPath: String,
    val thumbnailWidth: Int,
    val thumbnailHeight: Int,
    val scanlator: String,
    val uploadDate: Long,
    val tags: List<Tag>,
    val numPages: Int,
    val numFavorites: Int,
    val pages: List<PageInfo>,
    val related: List<GalleryListItem>? = null
)
