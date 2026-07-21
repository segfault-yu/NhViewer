package com.example.nhviewer.data.remote

import com.example.nhviewer.data.remote.dto.GalleryListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {
    @GET("api/v2/search")
    suspend fun searchGalleries(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("sort") sort: String? = null
    ): GalleryListResponse
}
