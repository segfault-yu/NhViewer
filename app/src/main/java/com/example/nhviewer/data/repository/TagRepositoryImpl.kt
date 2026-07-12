package com.example.nhviewer.data.repository

import com.example.nhviewer.data.remote.TagApi
import com.example.nhviewer.data.remote.dto.TagAutocompleteRequest
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val api: TagApi
) : TagRepository {

    override suspend fun autocompleteTags(query: String, type: String?): Result<List<Tag>> = runCatching {
        api.autocompleteTags(TagAutocompleteRequest(query, type)).map { it.toDomain() }
    }

    override suspend fun getTags(tagType: String, page: Int, sort: String): Result<List<Tag>> = runCatching {
        api.getTags(tagType, page, sort).result.map { it.toDomain() }
    }

    override suspend fun getTagDetail(tagType: String, slug: String): Result<Tag> = runCatching {
        api.getTagDetail(tagType, slug).toDomain()
    }

    override suspend fun getGalleriesTagged(tagId: Int, page: Int): Result<List<GalleryListItem>> = runCatching {
        api.getGalleriesTagged(tagId, page).result.map { it.toDomain() }
    }
}
