package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.FavoriteRepository
import javax.inject.Inject

class CheckIsFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(galleryId: Int): Result<Boolean> {
        return repository.checkIsFavorite(galleryId)
    }
}
