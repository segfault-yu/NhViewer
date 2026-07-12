package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository
import javax.inject.Inject

class GetTagDetailUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(tagType: String, slug: String): Result<Tag> {
        return repository.getTagDetail(tagType, slug)
    }
}
