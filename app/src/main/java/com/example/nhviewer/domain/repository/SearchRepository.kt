package com.example.nhviewer.domain.repository

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.PaginatedResult
import com.example.nhviewer.domain.model.SearchHistory
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun searchGalleries(query: String, page: Int, sort: String): Result<PaginatedResult<GalleryListItem>>
    fun getSearchHistory(): Flow<List<SearchHistory>>
    suspend fun insertSearchHistory(query: String)
    suspend fun deleteSearchHistory(query: String)
    suspend fun clearAllSearchHistory()
}
