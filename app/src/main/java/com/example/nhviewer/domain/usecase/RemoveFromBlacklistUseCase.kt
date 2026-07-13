package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.BlacklistRepository
import javax.inject.Inject

class RemoveFromBlacklistUseCase @Inject constructor(
    private val repository: BlacklistRepository
) {
    suspend operator fun invoke(tagId: Int): Result<Unit> {
        return repository.removeFromBlacklist(tagId)
    }
}
