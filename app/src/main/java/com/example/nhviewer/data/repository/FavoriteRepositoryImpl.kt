package com.example.nhviewer.data.repository

import com.example.nhviewer.data.local.dao.FavoriteCacheDao
import com.example.nhviewer.data.local.entity.FavoriteCacheEntity
import com.example.nhviewer.data.local.entity.toDomain
import com.example.nhviewer.data.remote.FavoriteApi
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.FavoriteRepository
import com.example.nhviewer.util.runCatchingCancelable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val api: FavoriteApi,
    private val dao: FavoriteCacheDao
) : FavoriteRepository {

    override val favoritesFlow: Flow<List<GalleryListItem>> = dao.getFavoritesFlow().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun getFavorites(page: Int): Result<List<GalleryListItem>> = runCatchingCancelable {
        try {
            val response = api.getFavorites(page)
            val items = response.result.map { it.toDomain() }
            
            // Sync with local cache
            val now = System.currentTimeMillis()
            items.forEach { item ->
                dao.insertFavorite(
                    FavoriteCacheEntity(
                        galleryId = item.id,
                        mediaId = item.mediaId,
                        englishTitle = item.englishTitle,
                        japaneseTitle = item.japaneseTitle,
                        thumbnail = item.thumbnail,
                        thumbnailWidth = item.thumbnailWidth,
                        thumbnailHeight = item.thumbnailHeight,
                        numPages = item.numPages,
                        numFavorites = item.numFavorites,
                        syncStatus = 0, // Synced
                        timestamp = now
                    )
                )
            }

            // Cleanup local DB on page 1: if an item is SYNCED in DB but not returned in page 1 and not present in newer pages (or simple purge),
            // Cleanup local DB on page 1
            if (page == 1) {
                val apiIds = items.map { it.id }.toSet()
                val cachedSynced = dao.getFavoritesBySyncStatus(0)
                cachedSynced.forEach { cached ->
                    if (cached.galleryId !in apiIds) {
                        dao.deleteFavorite(cached)
                    }
                }
            }
            items
        } catch (e: IOException) {
            // Offline fallback
            dao.getFavorites().map { it.toDomain() }
        }
    }

    override suspend fun getRandomFavorite(): Result<GalleryListItem> = runCatchingCancelable {
        try {
            api.getRandomFavorite().toDomain()
        } catch (e: IOException) {
            // Local fallback
            val local = dao.getFavorites()
            if (local.isEmpty()) {
                throw NoSuchElementException("本地收藏夹为空")
            }
            local.random().toDomain()
        }
    }

    override suspend fun checkIsFavorite(galleryId: Int): Result<Boolean> = runCatchingCancelable {
        val cached = dao.getFavoriteById(galleryId)
        if (cached != null) {
            cached.syncStatus != 2 // Not pending delete
        } else {
            try {
                val response = api.checkIsFavorite(galleryId)
                if (response.isFavorite) {
                    // Fetch details or mock minimal cache
                    // (just insert simple placeholder, full detail can be populated later)
                    // We don't have full data here, so we will not cache it until user favorites it or we fetch list.
                }
                response.isFavorite
            } catch (e: IOException) {
                false
            }
        }
    }

    override suspend fun toggleFavorite(gallery: GalleryListItem, isFavorite: Boolean): Result<Unit> = runCatchingCancelable {
        val now = System.currentTimeMillis()
        if (isFavorite) {
            val entity = FavoriteCacheEntity(
                galleryId = gallery.id,
                mediaId = gallery.mediaId,
                englishTitle = gallery.englishTitle,
                japaneseTitle = gallery.japaneseTitle,
                thumbnail = gallery.thumbnail,
                thumbnailWidth = gallery.thumbnailWidth,
                thumbnailHeight = gallery.thumbnailHeight,
                numPages = gallery.numPages,
                numFavorites = gallery.numFavorites,
                syncStatus = 1, // Pending Add
                timestamp = now
            )
            dao.insertFavorite(entity)
            try {
                val response = api.addFavorite(gallery.id)
                if (response.isSuccessful) {
                    dao.insertFavorite(entity.copy(syncStatus = 0))
                }
            } catch (e: IOException) {
                // Keep as pending add for offline sync
            }
        } else {
            val cached = dao.getFavoriteById(gallery.id)
            if (cached != null) {
                if (cached.syncStatus == 1) {
                    // Was pending add, just delete locally
                    dao.deleteFavorite(cached)
                } else {
                    // Mark as pending delete
                    val deleteEntity = cached.copy(syncStatus = 2, timestamp = now)
                    dao.insertFavorite(deleteEntity)
                    try {
                        val response = api.removeFavorite(gallery.id)
                        if (response.isSuccessful) {
                            dao.deleteFavorite(deleteEntity)
                        }
                    } catch (e: IOException) {
                        // Keep as pending delete for offline sync
                    }
                }
            } else {
                // Not in cache, try directly calling API
                api.removeFavorite(gallery.id)
            }
        }
    }

    override suspend fun syncOfflineFavorites(): Result<Unit> = runCatchingCancelable {
        // Sync pending adds
        val pendingAdds = dao.getFavoritesBySyncStatus(1)
        pendingAdds.forEach { item ->
            try {
                val response = api.addFavorite(item.galleryId)
                if (response.isSuccessful) {
                    dao.insertFavorite(item.copy(syncStatus = 0))
                }
            } catch (e: IOException) {
                // Ignore, retry later
            }
        }

        // Sync pending deletes
        val pendingDeletes = dao.getFavoritesBySyncStatus(2)
        pendingDeletes.forEach { item ->
            try {
                val response = api.removeFavorite(item.galleryId)
                if (response.isSuccessful) {
                    dao.deleteFavorite(item)
                }
            } catch (e: IOException) {
                // Ignore, retry later
            }
        }
    }
}
