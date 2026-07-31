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
    data class Search(val initialQuery: String? = null) : Route

    @Serializable
    data object Tags : Route

    @Serializable
    data object Auth : Route

    @Serializable
    data object Blacklist : Route

    @Serializable
    data object History : Route

    @Serializable
    data class GalleryDetail(val galleryId: Int) : Route

    @Serializable
    data class Reader(val galleryId: Int, val startPage: Int = 1) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Sessions : Route

    @Serializable
    data object ApiKeys : Route

    @Serializable
    data object About : Route
}
