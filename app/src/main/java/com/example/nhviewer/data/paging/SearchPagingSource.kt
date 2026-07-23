package com.example.nhviewer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.SearchRepository

class SearchPagingSource(
    private val repository: SearchRepository,
    private val query: String,
    private val sort: String,
    private val onTotalRetrieved: ((Int?) -> Unit)? = null
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
            val result = repository.searchGalleries(query, page, sort).getOrThrow()
            if (page == 1) {
                onTotalRetrieved?.invoke(result.total)
            }
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
