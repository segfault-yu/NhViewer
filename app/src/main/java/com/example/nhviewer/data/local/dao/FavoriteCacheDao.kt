package com.example.nhviewer.data.local.dao

import androidx.room.*
import com.example.nhviewer.data.local.entity.FavoriteCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCacheDao {
    @Query("SELECT * FROM favorite_cache WHERE syncStatus != 2 ORDER BY timestamp DESC")
    fun getFavoritesFlow(): Flow<List<FavoriteCacheEntity>>

    @Query("SELECT * FROM favorite_cache WHERE syncStatus != 2 ORDER BY timestamp DESC")
    suspend fun getFavorites(): List<FavoriteCacheEntity>

    @Query("SELECT * FROM favorite_cache WHERE syncStatus = :status")
    suspend fun getFavoritesBySyncStatus(status: Int): List<FavoriteCacheEntity>

    @Query("SELECT * FROM favorite_cache WHERE galleryId = :galleryId LIMIT 1")
    suspend fun getFavoriteById(galleryId: Int): FavoriteCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteCacheEntity)

    @Update
    suspend fun updateFavorite(entity: FavoriteCacheEntity)

    @Delete
    suspend fun deleteFavorite(entity: FavoriteCacheEntity)

    @Query("DELETE FROM favorite_cache WHERE galleryId = :galleryId")
    suspend fun deleteFavoriteById(galleryId: Int)

    @Query("DELETE FROM favorite_cache WHERE syncStatus = 0")
    suspend fun clearSyncedFavorites()
}
