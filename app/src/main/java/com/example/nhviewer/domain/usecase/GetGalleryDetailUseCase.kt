package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

class GetGalleryDetailUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    suspend operator fun invoke(galleryId: Int, includeRelated: Boolean, forceRefresh: Boolean = false): Result<GalleryDetail> {
        return repository.getGalleryDetail(galleryId, includeRelated, forceRefresh)
    }
}
