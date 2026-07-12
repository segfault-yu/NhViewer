package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.PageInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageInfoDto(
    @SerialName("number") val number: Int,
    @SerialName("path") val path: String,
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("thumbnail") val thumbnail: String,
    @SerialName("thumbnail_width") val thumbnailWidth: Int,
    @SerialName("thumbnail_height") val thumbnailHeight: Int
)

fun PageInfoDto.toDomain(): PageInfo = PageInfo(
    number = number,
    path = path,
    width = width,
    height = height,
    thumbnail = thumbnail,
    thumbnailWidth = thumbnailWidth,
    thumbnailHeight = thumbnailHeight
)
