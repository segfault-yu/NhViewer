package com.example.nhviewer.data.remote

import com.example.nhviewer.data.remote.dto.GalleryListResponse
import com.example.nhviewer.data.remote.dto.TagAutocompleteRequest
import com.example.nhviewer.data.remote.dto.TagListResponse
import com.example.nhviewer.data.remote.dto.TagResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TagApi {
    @POST("api/v2/tags/search")
    suspend fun autocompleteTags(
        @Body request: TagAutocompleteRequest
    ): List<TagResponseDto>

    @GET("api/v2/tags/{tag_type}")
    suspend fun getTags(
        @Path("tag_type") tagType: String,
        @Query("page") page: Int,
        @Query("sort") sort: String
    ): TagListResponse

    @GET("api/v2/tags/{tag_type}/{slug}")
    suspend fun getTagDetail(
        @Path("tag_type") tagType: String,
        @Path("slug") slug: String
    ): TagResponseDto

    @GET("api/v2/galleries/tagged")
    suspend fun getGalleriesTagged(
        @Query("tag_id") tagId: Int,
        @Query("page") page: Int
    ): GalleryListResponse
}
