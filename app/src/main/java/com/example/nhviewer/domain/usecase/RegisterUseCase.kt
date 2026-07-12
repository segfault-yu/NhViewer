package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.User
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<User> {
        return repository.register(username, email, password, powChallenge, powNonce, captchaResponse)
    }
}
