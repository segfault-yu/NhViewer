package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class RevokeSessionUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Unit> {
        return repository.revokeSession(sessionId)
    }
}
