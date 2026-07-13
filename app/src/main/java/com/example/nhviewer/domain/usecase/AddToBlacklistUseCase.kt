package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.BlacklistRepository
import javax.inject.Inject

class AddToBlacklistUseCase @Inject constructor(
    private val repository: BlacklistRepository
) {
    suspend operator fun invoke(tag: Tag): Result<Unit> {
        return repository.addToBlacklist(tag)
    }
}
