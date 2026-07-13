package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(gallery: GalleryListItem, isFavorite: Boolean): Result<Unit> {
        return repository.toggleFavorite(gallery, isFavorite)
    }
}
