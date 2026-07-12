package com.example.nhviewer.data.repository

import com.example.nhviewer.data.local.dao.SearchHistoryDao
import com.example.nhviewer.data.local.entity.SearchHistoryEntity
import com.example.nhviewer.data.local.entity.toDomain
import com.example.nhviewer.data.remote.SearchApi
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.nhviewer.util.runCatchingCancelable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val api: SearchApi,
    private val historyDao: SearchHistoryDao
) : SearchRepository {

    override suspend fun searchGalleries(query: String, page: Int, sort: String): Result<List<GalleryListItem>> = runCatchingCancelable {
        api.searchGalleries(query, page, sort).result.map { it.toDomain() }
    }

    override fun getSearchHistory(): Flow<List<SearchHistory>> {
        return historyDao.getSearchHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertSearchHistory(query: String) {
        if (query.isNotBlank()) {
            historyDao.insertOrUpdate(SearchHistoryEntity(query.trim(), System.currentTimeMillis()))
        }
    }

    override suspend fun deleteSearchHistory(query: String) {
        historyDao.deleteHistory(query)
    }

    override suspend fun clearAllSearchHistory() {
        historyDao.clearAll()
    }
}
