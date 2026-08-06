package com.example.nhviewer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nhviewer.data.local.dao.ReadingHistoryDao
import com.example.nhviewer.data.local.dao.SearchHistoryDao
import com.example.nhviewer.data.local.dao.FavoriteCacheDao
import com.example.nhviewer.data.local.dao.BlacklistTagDao
import com.example.nhviewer.data.local.entity.ReadingHistoryEntity
import com.example.nhviewer.data.local.entity.SearchHistoryEntity
import com.example.nhviewer.data.local.entity.FavoriteCacheEntity
import com.example.nhviewer.data.local.entity.BlacklistTagEntity

@Database(
    entities = [
        ReadingHistoryEntity::class,
        SearchHistoryEntity::class,
        FavoriteCacheEntity::class,
        BlacklistTagEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class NhViewerDatabase : RoomDatabase() {
    abstract val readingHistoryDao: ReadingHistoryDao
    abstract val searchHistoryDao: SearchHistoryDao
    abstract val favoriteCacheDao: FavoriteCacheDao
    abstract val blacklistTagDao: BlacklistTagDao
}
