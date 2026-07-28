package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.CachedGalleryDetail
import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

/** 取详情内存缓存及其时间戳，用于判定是否需要静默刷新，纯内存读取不涉及 IO */
class GetCachedGalleryDetailUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    operator fun invoke(galleryId: Int): CachedGalleryDetail? = repository.getCachedDetail(galleryId)
}
