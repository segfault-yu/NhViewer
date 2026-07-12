package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.UserSession
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class GetSessionsUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<List<UserSession>> {
        return repository.getSessions()
    }
}
