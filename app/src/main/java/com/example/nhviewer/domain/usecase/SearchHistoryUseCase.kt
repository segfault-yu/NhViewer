package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchHistoryUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    fun getSearchHistory(): Flow<List<SearchHistory>> {
        return repository.getSearchHistory()
    }

    suspend fun addSearchHistory(query: String) {
        repository.insertSearchHistory(query)
    }

    suspend fun removeSearchHistory(query: String) {
        repository.deleteSearchHistory(query)
    }

    suspend fun clearSearchHistory() {
        repository.clearAllSearchHistory()
    }
}
