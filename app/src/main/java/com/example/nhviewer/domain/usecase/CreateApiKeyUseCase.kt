package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.ApiKey
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class CreateApiKeyUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        powChallenge: String,
        powNonce: String,
        captchaResponse: String
    ): Result<ApiKey> {
        return repository.createApiKey(name, powChallenge, powNonce, captchaResponse)
    }
}
