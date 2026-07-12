package com.example.nhviewer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nhviewer.data.local.entity.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ReadingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: ReadingHistoryEntity)

    @Query("UPDATE reading_history SET last_read_page = :lastReadPage, timestamp = :timestamp WHERE gallery_id = :galleryId")
    suspend fun updatePage(galleryId: Int, lastReadPage: Int, timestamp: Long)

    @Query("SELECT * FROM reading_history WHERE gallery_id = :galleryId LIMIT 1")
    suspend fun getHistoryItem(galleryId: Int): ReadingHistoryEntity?
}
