package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class RevokeApiKeyUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(keyId: String): Result<Unit> {
        return repository.revokeApiKey(keyId)
    }
}
