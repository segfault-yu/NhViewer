package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.GalleryDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GalleryDetailResponse(
    @SerialName("id") val id: Int,
    @SerialName("media_id") val mediaId: String,
    @SerialName("title") val title: GalleryTitleDto,
    @SerialName("cover") val cover: CoverInfoDto,
    @SerialName("thumbnail") val thumbnail: CoverInfoDto,
    @SerialName("scanlator") val scanlator: String = "",
    @SerialName("upload_date") val uploadDate: Long,
    @SerialName("tags") val tags: List<TagResponseDto> = emptyList(),
    @SerialName("num_pages") val numPages: Int,
    @SerialName("num_favorites") val numFavorites: Int,
    @SerialName("pages") val pages: List<PageInfoDto> = emptyList(),
    @SerialName("related") val related: List<GalleryListItemDto>? = null
)

@Serializable
data class GalleryTitleDto(
    @SerialName("english") val english: String,
    @SerialName("japanese") val japanese: String? = null,
    @SerialName("pretty") val pretty: String? = null
)

@Serializable
data class CoverInfoDto(
    @SerialName("path") val path: String,
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int
)

fun GalleryDetailResponse.toDomain(): GalleryDetail = GalleryDetail(
    id = id,
    mediaId = mediaId,
    englishTitle = title.english,
    japaneseTitle = title.japanese,
    prettyTitle = title.pretty,
    coverPath = cover.path,
    coverWidth = cover.width,
    coverHeight = cover.height,
    thumbnailPath = thumbnail.path,
    thumbnailWidth = thumbnail.width,
    thumbnailHeight = thumbnail.height,
    scanlator = scanlator,
    uploadDate = uploadDate,
    tags = tags.map { it.toDomain() },
    numPages = numPages,
    numFavorites = numFavorites,
    pages = pages.map { it.toDomain() },
    related = related?.map { it.toDomain() }
)
