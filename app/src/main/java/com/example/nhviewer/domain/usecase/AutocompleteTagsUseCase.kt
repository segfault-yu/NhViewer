package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository
import javax.inject.Inject

class AutocompleteTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(query: String, type: String? = null): Result<List<Tag>> {
        return repository.autocompleteTags(query, type)
    }
}
