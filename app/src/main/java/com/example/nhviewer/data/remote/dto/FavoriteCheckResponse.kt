package com.example.nhviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteCheckResponse(
    @SerialName("favorited") val isFavorite: Boolean = false
)
