package com.example.nhviewer.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Favorites : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class GalleryDetail(val galleryId: Int) : Route

    @Serializable
    data class Reader(val galleryId: Int, val startPage: Int = 1) : Route
}
