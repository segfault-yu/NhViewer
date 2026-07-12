package com.example.nhviewer.domain.usecase

import com.example.nhviewer.data.remote.dto.PowResponseDto
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class GetPowChallengeUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(action: String): Result<PowResponseDto> {
        return repository.getPowChallenge(action)
    }
}
