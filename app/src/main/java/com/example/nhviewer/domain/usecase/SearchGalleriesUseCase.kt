package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.PaginatedResult
import com.example.nhviewer.domain.repository.SearchRepository
import javax.inject.Inject

class SearchGalleriesUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    suspend operator fun invoke(query: String, page: Int, sort: String): Result<PaginatedResult<GalleryListItem>> {
        return repository.searchGalleries(query, page, sort)
    }
}
