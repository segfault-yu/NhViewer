package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.logout()
    }
}
