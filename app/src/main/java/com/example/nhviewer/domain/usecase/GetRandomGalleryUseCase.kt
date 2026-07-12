package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

class GetRandomGalleryUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.getRandomGalleryId()
    }
}
