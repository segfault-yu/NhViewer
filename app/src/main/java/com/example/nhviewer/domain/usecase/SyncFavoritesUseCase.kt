package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.FavoriteRepository
import javax.inject.Inject

class SyncFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncOfflineFavorites()
    }
}
