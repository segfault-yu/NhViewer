package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

class GetPopularGalleriesUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    suspend operator fun invoke(): Result<List<GalleryListItem>> {
        return repository.getPopularGalleries()
    }
}
