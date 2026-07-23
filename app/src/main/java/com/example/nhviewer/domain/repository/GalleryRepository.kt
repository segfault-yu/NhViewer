package com.example.nhviewer.domain.repository

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

    fun getReadingHistory(): Flow<List<ReadingHistory>>
    suspend fun saveReadingHistory(history: ReadingHistory)
    suspend fun updateReadingPage(galleryId: Int, lastReadPage: Int, timestamp: Long)
    suspend fun getReadingHistoryItem(galleryId: Int): ReadingHistory?
}
