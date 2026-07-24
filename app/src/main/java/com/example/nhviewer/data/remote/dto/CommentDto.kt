package com.example.nhviewer.data.remote.dto

import com.example.nhviewer.domain.model.Comment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    @SerialName("id") val id: Int,
    @SerialName("gallery_id") val galleryId: Int,
    @SerialName("poster") val poster: UserInfoDto,
    @SerialName("post_date") val postDate: Long,
    @SerialName("body") val body: String
)

@Serializable
data class CommentListResponse(
    @SerialName("result") val result: List<CommentDto>,
    @SerialName("num_pages") val numPages: Int,
    @SerialName("per_page") val perPage: Int = 25,
    @SerialName("total") val total: Int? = null
)

fun CommentDto.toDomain() = Comment(
    id = id,
    galleryId = galleryId,
    username = poster.username,
    avatarUrl = if (poster.avatarUrl.startsWith("//")) "https:${poster.avatarUrl}" else poster.avatarUrl,
    userId = poster.id,
    postDate = postDate,
    body = body
)
