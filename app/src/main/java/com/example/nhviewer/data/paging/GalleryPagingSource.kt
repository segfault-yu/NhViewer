package com.example.nhviewer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.GalleryRepository

class GalleryPagingSource(
    private val repository: GalleryRepository
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
            val response = repository.getGalleries(page).getOrThrow()
            LoadResult.Page(
                data = response,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
