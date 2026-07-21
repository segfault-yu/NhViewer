package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.model.PaginatedResult
import com.example.nhviewer.domain.repository.TagRepository
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(tagType: String, page: Int, sort: String): Result<PaginatedResult<Tag>> {
        return repository.getTags(tagType, page, sort)
    }
}
