package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("type") val type: String,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("url") val url: String,
    @SerialName("count") val count: Int,
    @SerialName("description") val description: String? = null,
    @SerialName("is_community") val isCommunity: Boolean? = null
)

fun TagResponseDto.toDomain(): Tag = Tag(
    id = id,
    type = type,
    name = name,
    slug = slug,
    url = url,
    count = count,
    description = description,
    isCommunity = isCommunity
)
