package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.ApiKey
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class GetApiKeysUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<List<ApiKey>> {
        return repository.getApiKeys()
    }
}
