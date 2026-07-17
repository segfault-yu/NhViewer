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
    data object Search : Route

    @Serializable
    data object Tags : Route

    @Serializable
    data object Auth : Route

    @Serializable
    data object Blacklist : Route

    @Serializable
    data object History : Route

    @Serializable
    data class TaggedGalleries(val tagId: Int, val tagName: String) : Route

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
}
