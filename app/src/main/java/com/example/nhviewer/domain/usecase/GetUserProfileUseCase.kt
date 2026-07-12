package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.User
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return repository.loadCurrentUser()
    }
}
