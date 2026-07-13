package com.example.nhviewer.data.local.dao

import androidx.room.*
import com.example.nhviewer.data.local.entity.BlacklistTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistTagDao {
    @Query("SELECT * FROM blacklist_tag ORDER BY name ASC")
    fun getBlacklistFlow(): Flow<List<BlacklistTagEntity>>

    @Query("SELECT * FROM blacklist_tag ORDER BY name ASC")
    suspend fun getBlacklist(): List<BlacklistTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: BlacklistTagEntity)

    @Delete
    suspend fun deleteTag(tag: BlacklistTagEntity)

    @Query("DELETE FROM blacklist_tag WHERE tagId = :tagId")
    suspend fun deleteTagById(tagId: Int)

    @Query("DELETE FROM blacklist_tag")
    suspend fun clearAll()
}
