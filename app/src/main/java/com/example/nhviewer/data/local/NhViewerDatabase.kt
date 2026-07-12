package com.example.nhviewer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nhviewer.data.local.dao.ReadingHistoryDao
import com.example.nhviewer.data.local.entity.ReadingHistoryEntity

@Database(entities = [ReadingHistoryEntity::class], version = 1, exportSchema = false)
abstract class NhViewerDatabase : RoomDatabase() {
    abstract val readingHistoryDao: ReadingHistoryDao
}
