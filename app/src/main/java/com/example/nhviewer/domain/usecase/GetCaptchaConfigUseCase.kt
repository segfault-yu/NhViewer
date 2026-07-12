package com.example.nhviewer.domain.usecase

import com.example.nhviewer.data.remote.dto.CaptchaResponseDto
import com.example.nhviewer.domain.repository.UserRepository
import javax.inject.Inject

class GetCaptchaConfigUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<CaptchaResponseDto> {
        return repository.getCaptchaConfig()
    }
}
