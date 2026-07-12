package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.TagRepository
import javax.inject.Inject

class GetGalleriesTaggedUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(tagId: Int, page: Int): Result<List<GalleryListItem>> {
        return repository.getGalleriesTagged(tagId, page)
    }
}
