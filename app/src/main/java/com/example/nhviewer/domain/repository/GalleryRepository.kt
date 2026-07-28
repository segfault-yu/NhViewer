package com.example.nhviewer.domain.repository

import com.example.nhviewer.domain.model.CachedGalleryDetail
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.PaginatedResult
import com.example.nhviewer.domain.model.ReadingHistory
import kotlinx.coroutines.flow.Flow

interface GalleryRepository {
    suspend fun getGalleries(page: Int, forceRefresh: Boolean = false): Result<PaginatedResult<GalleryListItem>>
    suspend fun getPopularGalleries(forceRefresh: Boolean = false): Result<List<GalleryListItem>>
    suspend fun getRandomGalleryId(): Result<Int>
    suspend fun getGalleryDetail(galleryId: Int, includeRelated: Boolean, forceRefresh: Boolean = false): Result<GalleryDetail>
    suspend fun getRelatedGalleries(galleryId: Int): Result<List<GalleryListItem>>
    suspend fun getCdnConfig(): Result<CdnConfig>

    /** 读取列表快照，供详情页预填首屏；无快照时返回 null */
    fun getCachedPreview(galleryId: Int): GalleryListItem?

    /** 读取详情内存缓存，供命中后判定是否需要静默刷新；无缓存时返回 null */
    fun getCachedDetail(galleryId: Int): CachedGalleryDetail?

    fun getReadingHistory(): Flow<List<ReadingHistory>>
    suspend fun saveReadingHistory(history: ReadingHistory)
    suspend fun updateReadingPage(galleryId: Int, lastReadPage: Int, timestamp: Long)
    suspend fun getReadingHistoryItem(galleryId: Int): ReadingHistory?
}
