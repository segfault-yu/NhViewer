package com.example.nhviewer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.TagRepository
import com.example.nhviewer.util.log.AppLogger
import kotlinx.coroutines.CancellationException

class TaggedGalleryPagingSource(
    private val repository: TagRepository,
    private val tagId: Int
) : PagingSource<Int, GalleryListItem>() {

    // 记录本次分页会话已返回的画廊 id：新画廊持续插入会让相邻页码的偏移窗口重叠，
    // 同一 id 可能在两页里都出现，导致 LazyStaggeredGrid 的 key 冲突崩溃
    private val seenIds = mutableSetOf<Int>()

    override fun getRefreshKey(state: PagingState<Int, GalleryListItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryListItem> {
        val page = params.key ?: 1
        return try {
            val result = repository.getGalleriesTagged(tagId, page).getOrThrow()
            LoadResult.Page(
                data = result.items.filter { seenIds.add(it.id) },
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= result.numPages || result.items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            if (e !is CancellationException) {
                AppLogger.w("TaggedPaging", "标签 $tagId 的画廊第 $page 页加载失败", e)
            }
            LoadResult.Error(e)
        }
    }
}
