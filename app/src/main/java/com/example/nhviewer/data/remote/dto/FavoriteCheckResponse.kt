package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteCheckResponse(
    // 服务端字段已改为 favorited，is_favorite 不再下发
    @SerialName("favorited") val isFavorite: Boolean = false
)
