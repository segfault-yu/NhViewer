package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

class GetPopularGalleriesUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<GalleryListItem>> {
        return repository.getPopularGalleries(forceRefresh)
    }
}
