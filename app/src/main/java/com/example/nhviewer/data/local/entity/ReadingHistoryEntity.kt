package com.example.nhviewer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nhviewer.domain.model.ReadingHistory

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "gallery_id") val galleryId: Int,
    @ColumnInfo(name = "media_id") val mediaId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "last_read_page") val lastReadPage: Int,
    @ColumnInfo(name = "total_pages") val totalPages: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)

fun ReadingHistoryEntity.toDomain() = ReadingHistory(
    galleryId = galleryId,
    mediaId = mediaId,
    title = title,
    lastReadPage = lastReadPage,
    totalPages = totalPages,
    timestamp = timestamp
)

fun ReadingHistory.toEntity() = ReadingHistoryEntity(
    galleryId = galleryId,
    mediaId = mediaId,
    title = title,
    lastReadPage = lastReadPage,
    totalPages = totalPages,
    timestamp = timestamp
)
