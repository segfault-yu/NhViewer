package com.example.nhviewer.data.local

import android.util.LruCache
import com.example.nhviewer.domain.model.CachedGalleryDetail
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 画廊数据的进程内缓存。
 * 列表快照供详情页预填首屏，详情与推荐快照避免返回列表后重复发起请求。
 */
@Singleton
class GalleryMemoryCache @Inject constructor() {

    private val previewCache = LruCache<Int, GalleryListItem>(PREVIEW_CAPACITY)
    private val detailCache = LruCache<Int, CachedGalleryDetail>(DETAIL_CAPACITY)
    private val relatedCache = LruCache<Int, List<GalleryListItem>>(DETAIL_CAPACITY)

    fun putPreviews(items: List<GalleryListItem>) {
        items.forEach { previewCache.put(it.id, it) }
    }

    fun getPreview(galleryId: Int): GalleryListItem? = previewCache.get(galleryId)

    fun putDetail(detail: GalleryDetail, relatedFetched: Boolean) {
        detailCache.put(
            detail.id,
            CachedGalleryDetail(
                detail = detail,
                cachedAt = System.currentTimeMillis(),
                relatedFetched = relatedFetched
            )
        )
        // 详情自带的推荐同样落快照，点进推荐条目时可直接预填首屏
        val related = detail.related
        if (!related.isNullOrEmpty()) {
            putRelated(detail.id, related)
        }
    }

    fun getDetail(galleryId: Int): CachedGalleryDetail? = detailCache.get(galleryId)

    fun putRelated(galleryId: Int, items: List<GalleryListItem>) {
        relatedCache.put(galleryId, items)
        putPreviews(items)
    }

    /** 返回 null 表示尚未请求过；返回空列表表示已确认该画廊无推荐 */
    fun getRelated(galleryId: Int): List<GalleryListItem>? = relatedCache.get(galleryId)

    /** 当前缓存的条目总数，用于设置页展示 */
    fun entryCount(): Int = previewCache.size() + detailCache.size() + relatedCache.size()

    fun clear() {
        previewCache.evictAll()
        detailCache.evictAll()
        relatedCache.evictAll()
    }

    private companion object {
        // 分页列表快速下拉时 Paging3 会连续预取多页，300 太容易被挤出导致点开详情时缓存已丢失
        const val PREVIEW_CAPACITY = 1000
        const val DETAIL_CAPACITY = 30
    }
}
