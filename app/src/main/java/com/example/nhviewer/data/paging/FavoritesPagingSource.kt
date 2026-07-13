package com.example.nhviewer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.usecase.GetFavoritesUseCase

class FavoritesPagingSource(
    private val getFavoritesUseCase: GetFavoritesUseCase
) : PagingSource<Int, GalleryListItem>() {

    override fun getRefreshKey(state: PagingState<Int, GalleryListItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryListItem> {
        val position = params.key ?: 1
        return try {
            val result = getFavoritesUseCase(position).getOrThrow()
            LoadResult.Page(
                data = result,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (result.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}
