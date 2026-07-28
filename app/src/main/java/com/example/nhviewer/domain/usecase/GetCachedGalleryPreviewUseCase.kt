package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.GalleryRepository
import javax.inject.Inject

/** 取列表快照用于详情页首屏预填，纯内存读取不涉及 IO */
class GetCachedGalleryPreviewUseCase @Inject constructor(
    private val repository: GalleryRepository
) {
    operator fun invoke(galleryId: Int): GalleryListItem? = repository.getCachedPreview(galleryId)
}
