package com.example.nhviewer.domain.repository

import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.PaginatedResult

interface TagRepository {
    suspend fun autocompleteTags(query: String, type: String?): Result<List<Tag>>
    suspend fun getTags(tagType: String, page: Int, sort: String): Result<PaginatedResult<Tag>>
    suspend fun getTagDetail(tagType: String, slug: String): Result<Tag>
    suspend fun getGalleriesTagged(tagId: Int, page: Int): Result<PaginatedResult<GalleryListItem>>
    suspend fun getTagsByIds(ids: List<Int>): Result<List<Tag>>
}
