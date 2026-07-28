package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

class GetCdnConfigUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    suspend operator fun invoke(): Result<CdnConfig> {
        return repository.getCdnConfig()
    }

    /** 同步取已缓存配置，用作 UI 状态初值 */
    fun cached(): CdnConfig = repository.getCachedCdnConfig()
}
