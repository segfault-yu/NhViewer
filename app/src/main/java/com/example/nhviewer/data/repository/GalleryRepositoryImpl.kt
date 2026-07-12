package com.example.nhviewer.data.repository

import com.example.nhviewer.data.local.dao.ReadingHistoryDao
import com.example.nhviewer.data.local.entity.toDomain
import com.example.nhviewer.data.local.entity.toEntity
import com.example.nhviewer.data.remote.GalleryApi
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepositoryImpl @Inject constructor(
    private val api: GalleryApi,
    private val historyDao: ReadingHistoryDao
) : GalleryRepository {

    override suspend fun getGalleries(page: Int): Result<List<GalleryListItem>> = runCatching {
        api.getGalleries(page).result.map { it.toDomain() }
    }

    override suspend fun getPopularGalleries(): Result<List<GalleryListItem>> = runCatching {
        api.getPopularGalleries().map { it.toDomain() }
    }

    override suspend fun getRandomGalleryId(): Result<Int> = runCatching {
        api.getRandomGallery()["id"] ?: throw NoSuchElementException("No random gallery ID returned")
    }

    override suspend fun getGalleryDetail(galleryId: Int, includeRelated: Boolean): Result<GalleryDetail> = runCatching {
        val includeStr = if (includeRelated) "related" else null
        api.getGalleryDetail(galleryId, includeStr).toDomain()
    }

    override suspend fun getRelatedGalleries(galleryId: Int): Result<List<GalleryListItem>> = runCatching {
        api.getRelatedGalleries(galleryId).result.map { it.toDomain() }
    }

    private val cdnMutex = Mutex()
    @Volatile
    private var cachedCdnConfig: CdnConfig? = null

    override suspend fun getCdnConfig(): Result<CdnConfig> {
        cachedCdnConfig?.let { return Result.success(it) }
        return cdnMutex.withLock {
            cachedCdnConfig?.let { return@withLock Result.success(it) }
            runCatching {
                api.getCdnConfig().toDomain().also {
                    cachedCdnConfig = it
                }
            }
        }
    }

    override fun getReadingHistory(): Flow<List<ReadingHistory>> {
        return historyDao.getAllHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveReadingHistory(history: ReadingHistory) {
        historyDao.insertOrUpdate(history.toEntity())
    }

    override suspend fun updateReadingPage(galleryId: Int, lastReadPage: Int, timestamp: Long) {
        historyDao.updatePage(galleryId, lastReadPage, timestamp)
    }

    override suspend fun getReadingHistoryItem(galleryId: Int): ReadingHistory? {
        return historyDao.getHistoryItem(galleryId)?.toDomain()
    }
}
