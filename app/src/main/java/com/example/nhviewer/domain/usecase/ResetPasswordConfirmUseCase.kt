package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class ResetPasswordConfirmUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        token: String,
        newPassword: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<Unit> {
        return repository.confirmPasswordReset(token, newPassword, powChallenge, powNonce, captchaResponse)
    }
}
