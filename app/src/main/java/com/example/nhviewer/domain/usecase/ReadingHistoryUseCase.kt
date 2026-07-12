package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadingHistoryUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    fun getReadingHistory(): Flow<List<ReadingHistory>> {
        return repository.getReadingHistory()
    }

    suspend fun saveReadingHistory(history: ReadingHistory) {
        repository.saveReadingHistory(history)
    }

    suspend fun updateReadingPage(galleryId: Int, lastReadPage: Int, timestamp: Long) {
        repository.updateReadingPage(galleryId, lastReadPage, timestamp)
    }

    suspend fun getReadingHistoryItem(galleryId: Int): ReadingHistory? {
        return repository.getReadingHistoryItem(galleryId)
    }
}
