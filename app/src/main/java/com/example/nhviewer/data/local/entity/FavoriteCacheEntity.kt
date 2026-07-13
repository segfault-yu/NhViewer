package com.example.nhviewer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nhviewer.domain.model.GalleryListItem

@Entity(tableName = "favorite_cache")
data class FavoriteCacheEntity(
    @PrimaryKey val galleryId: Int,
    val mediaId: String,
    val englishTitle: String,
    val japaneseTitle: String?,
    val thumbnail: String,
    val thumbnailWidth: Int,
    val thumbnailHeight: Int,
    val numPages: Int,
    val numFavorites: Int,
    val syncStatus: Int, // 0 = SYNCED, 1 = PENDING_ADD, 2 = PENDING_DELETE
    val timestamp: Long
)

fun FavoriteCacheEntity.toDomain() = GalleryListItem(
    id = galleryId,
    mediaId = mediaId,
    englishTitle = englishTitle,
    japaneseTitle = japaneseTitle,
    thumbnail = thumbnail,
    thumbnailWidth = thumbnailWidth,
    thumbnailHeight = thumbnailHeight,
    numPages = numPages,
    numFavorites = numFavorites,
    tagIds = emptyList(),
    blacklisted = false
)
