package com.example.nhviewer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nhviewer.domain.model.SearchHistory

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "query") val query: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)

fun SearchHistoryEntity.toDomain() = SearchHistory(
    query = query,
    timestamp = timestamp
)
