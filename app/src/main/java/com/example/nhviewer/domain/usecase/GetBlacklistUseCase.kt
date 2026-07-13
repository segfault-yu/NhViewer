package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.BlacklistRepository
import javax.inject.Inject

class GetBlacklistUseCase @Inject constructor(
    private val repository: BlacklistRepository
) {
    suspend operator fun invoke(): Result<List<Tag>> {
        return repository.getBlacklist()
    }
}
