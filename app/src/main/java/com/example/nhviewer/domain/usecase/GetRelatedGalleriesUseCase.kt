package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

class GetRelatedGalleriesUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    suspend operator fun invoke(galleryId: Int): Result<List<GalleryListItem>> {
        return repository.getRelatedGalleries(galleryId)
    }
}
