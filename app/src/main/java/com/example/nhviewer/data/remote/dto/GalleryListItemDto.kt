package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.GalleryListItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// API 返回的分页包装对象
@Serializable
data class GalleryListResponse(
    @SerialName("result") val result: List<GalleryListItemDto>,
    @SerialName("num_pages") val numPages: Int,
    @SerialName("per_page") val perPage: Int = 25,
    @SerialName("total") val total: Int? = null
)

@Serializable
data class RelatedGalleriesResponse(
    @SerialName("result") val result: List<GalleryListItemDto>
)

@Serializable
data class GalleryListItemDto(
    @SerialName("id") val id: Int,
    @SerialName("media_id") val mediaId: String,
    @SerialName("english_title") val englishTitle: String,
    @SerialName("japanese_title") val japaneseTitle: String? = null,
    @SerialName("thumbnail") val thumbnail: String,
    @SerialName("thumbnail_width") val thumbnailWidth: Int,
    @SerialName("thumbnail_height") val thumbnailHeight: Int,
    @SerialName("num_pages") val numPages: Int,
    @SerialName("num_favorites") val numFavorites: Int,
    @SerialName("tag_ids") val tagIds: List<Int> = emptyList(),
    @SerialName("blacklisted") val blacklisted: Boolean = false
)

fun GalleryListItemDto.toDomain(): GalleryListItem = GalleryListItem(
    id = id,
    mediaId = mediaId,
    englishTitle = englishTitle,
    japaneseTitle = japaneseTitle,
    thumbnail = thumbnail,
    thumbnailWidth = thumbnailWidth,
    thumbnailHeight = thumbnailHeight,
    numPages = numPages,
    numFavorites = numFavorites,
    tagIds = tagIds,
    blacklisted = blacklisted
)
