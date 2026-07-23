package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.FavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    private var lastSyncTimestamp = 0L

    suspend operator fun invoke(forceSync: Boolean = false): Result<Unit> {
        val now = System.currentTimeMillis()
        if (!forceSync && (now - lastSyncTimestamp < 5 * 60 * 1000L)) {
            return Result.success(Unit)
        }
        return repository.syncOfflineFavorites().also {
            if (it.isSuccess) {
                lastSyncTimestamp = System.currentTimeMillis()
            }
        }
    }
}
