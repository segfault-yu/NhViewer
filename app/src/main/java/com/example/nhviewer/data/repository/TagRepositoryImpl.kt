package com.example.nhviewer.data.repository

import com.example.nhviewer.data.local.GalleryMemoryCache
import com.example.nhviewer.data.remote.TagApi
import com.example.nhviewer.data.remote.dto.TagAutocompleteRequest
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.PaginatedResult
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository
import com.example.nhviewer.util.runCatchingCancelable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val api: TagApi,
    private val memoryCache: GalleryMemoryCache
) : TagRepository {

    override suspend fun autocompleteTags(query: String, type: String?): Result<List<Tag>> = runCatchingCancelable("Tag") {
        api.autocompleteTags(TagAutocompleteRequest(query, type)).map { it.toDomain() }
    }

    override suspend fun getTags(tagType: String, page: Int, sort: String): Result<PaginatedResult<Tag>> = runCatchingCancelable("Tag") {
        val response = api.getTags(tagType, page, sort)
        PaginatedResult(
            items = response.result.map { it.toDomain() },
            numPages = response.numPages,
            total = response.total
        )
    }

    override suspend fun getTagDetail(tagType: String, slug: String): Result<Tag> = runCatchingCancelable("Tag") {
        api.getTagDetail(tagType, slug).toDomain()
    }

    override suspend fun getGalleriesTagged(tagId: Int, page: Int): Result<PaginatedResult<GalleryListItem>> = runCatchingCancelable("Tag") {
        val response = api.getGalleriesTagged(tagId, page)
        val items = response.result.map { it.toDomain() }
        memoryCache.putPreviews(items)
        PaginatedResult(
            items = items,
            numPages = response.numPages,
            total = response.total
        )
    }

    override suspend fun getTagsByIds(ids: List<Int>): Result<List<Tag>> = runCatchingCancelable("Tag") {
        api.getTagsByIds(ids).map { it.toDomain() }
    }
}
