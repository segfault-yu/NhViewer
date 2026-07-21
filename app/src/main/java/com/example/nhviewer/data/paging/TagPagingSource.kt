package com.example.nhviewer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository

class TagPagingSource(
    private val repository: TagRepository,
    private val tagType: String,
    private val sort: String
) : PagingSource<Int, Tag>() {

    override fun getRefreshKey(state: PagingState<Int, Tag>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Tag> {
        val page = params.key ?: 1
        return try {
            val result = repository.getTags(tagType, page, sort).getOrThrow()
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
