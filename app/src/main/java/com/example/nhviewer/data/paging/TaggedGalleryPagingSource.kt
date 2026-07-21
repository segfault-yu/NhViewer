package com.example.nhviewer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.TagRepository

class TaggedGalleryPagingSource(
    private val repository: TagRepository,
    private val tagId: Int
) : PagingSource<Int, GalleryListItem>() {

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
                data = result.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page >= result.numPages || result.items.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
