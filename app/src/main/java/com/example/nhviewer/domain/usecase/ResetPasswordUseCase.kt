package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        usernameOrEmail: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<Unit> {
        return repository.requestPasswordReset(usernameOrEmail, powChallenge, powNonce, captchaResponse)
    }
}
