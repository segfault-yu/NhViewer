package com.example.nhviewer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nhviewer.data.local.dao.ReadingHistoryDao
import com.example.nhviewer.data.local.dao.SearchHistoryDao
import com.example.nhviewer.data.local.entity.ReadingHistoryEntity
import com.example.nhviewer.data.local.entity.SearchHistoryEntity

@Database(
    entities = [ReadingHistoryEntity::class, SearchHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NhViewerDatabase : RoomDatabase() {
    abstract val readingHistoryDao: ReadingHistoryDao
    abstract val searchHistoryDao: SearchHistoryDao
}
