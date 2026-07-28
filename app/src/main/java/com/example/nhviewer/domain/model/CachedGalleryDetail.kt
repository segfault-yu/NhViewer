package com.example.nhviewer.domain.model

/**
 * 详情内存缓存条目。
 * cachedAt 用于判定是否需要后台静默刷新；relatedFetched 标记推荐是否已随详情请求过，
 * 避免服务端返回空推荐时缓存被永久判定为失效。
 */
data class CachedGalleryDetail(
    val detail: GalleryDetail,
    val cachedAt: Long,
    val relatedFetched: Boolean
)
