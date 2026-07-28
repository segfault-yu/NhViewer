package com.example.nhviewer.data.repository

import com.example.nhviewer.data.local.GalleryMemoryCache
import com.example.nhviewer.data.local.dao.ReadingHistoryDao
import com.example.nhviewer.data.local.entity.toDomain
import com.example.nhviewer.data.local.entity.toEntity
import com.example.nhviewer.data.remote.GalleryApi
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.CachedGalleryDetail
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.PaginatedResult
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.nhviewer.util.runCatchingCancelable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepositoryImpl @Inject constructor(
    private val api: GalleryApi,
    private val historyDao: ReadingHistoryDao,
    private val memoryCache: GalleryMemoryCache
) : GalleryRepository {

    override suspend fun getGalleries(page: Int, forceRefresh: Boolean): Result<PaginatedResult<GalleryListItem>> = runCatchingCancelable("Gallery") {
        val forceHeader = if (forceRefresh) "true" else null
        val response = api.getGalleries(page, forceHeader)
        val items = response.result.map { it.toDomain() }
        memoryCache.putPreviews(items)
        PaginatedResult(
            items = items,
            numPages = response.numPages,
            total = response.total
        )
    }

    override suspend fun getPopularGalleries(forceRefresh: Boolean): Result<List<GalleryListItem>> = runCatchingCancelable("Gallery") {
        val forceHeader = if (forceRefresh) "true" else null
        api.getPopularGalleries(forceHeader).map { it.toDomain() }.also {
            memoryCache.putPreviews(it)
        }
    }

    override suspend fun getRandomGalleryId(): Result<Int> = runCatchingCancelable("Gallery") {
        api.getRandomGallery()["id"] ?: throw NoSuchElementException("No random gallery ID returned")
    }

    override suspend fun getGalleryDetail(galleryId: Int, includeRelated: Boolean, forceRefresh: Boolean): Result<GalleryDetail> = runCatchingCancelable("Gallery") {
        if (!forceRefresh) {
            // relatedFetched 而非 related 是否为空：服务端返回空推荐时缓存依然有效
            val cached = memoryCache.getDetail(galleryId)
            if (cached != null && (!includeRelated || cached.relatedFetched)) {
                return@runCatchingCancelable cached.detail
            }
        }
        val includeStr = if (includeRelated) "related" else null
        val forceHeader = if (forceRefresh) "true" else null
        api.getGalleryDetail(galleryId, includeStr, forceHeader).toDomain().also {
            memoryCache.putDetail(it, relatedFetched = includeRelated)
        }
    }

    override suspend fun getRelatedGalleries(galleryId: Int): Result<List<GalleryListItem>> = runCatchingCancelable("Gallery") {
        memoryCache.getRelated(galleryId)?.let { return@runCatchingCancelable it }
        api.getRelatedGalleries(galleryId).result.map { it.toDomain() }.also {
            memoryCache.putRelated(galleryId, it)
        }
    }

    override fun getCachedPreview(galleryId: Int): GalleryListItem? = memoryCache.getPreview(galleryId)

    override fun getCachedDetail(galleryId: Int): CachedGalleryDetail? = memoryCache.getDetail(galleryId)

    private val cdnMutex = Mutex()
    @Volatile
    private var cachedCdnConfig: CdnConfig? = null

    override suspend fun getCdnConfig(): Result<CdnConfig> {
        cachedCdnConfig?.let { return Result.success(it) }
        return cdnMutex.withLock {
            cachedCdnConfig?.let { return@withLock Result.success(it) }
            runCatchingCancelable("Gallery") {
                api.getCdnConfig().toDomain().also {
                    cachedCdnConfig = it
                }
            }
        }
    }

    override fun getCachedCdnConfig(): CdnConfig = cachedCdnConfig ?: CdnConfig.DEFAULT

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
