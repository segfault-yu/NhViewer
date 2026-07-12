package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.User
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<User> {
        return repository.login(username, password, powChallenge, powNonce, captchaResponse)
    }
}
