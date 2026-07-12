package com.example.nhviewer.data.remote

import com.example.nhviewer.data.remote.dto.GalleryDetailResponse
import com.example.nhviewer.data.remote.dto.GalleryListItemDto
import com.example.nhviewer.data.remote.dto.GalleryListResponse
import com.example.nhviewer.data.remote.dto.RelatedGalleriesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GalleryApi {
    @GET("api/v2/galleries")
    suspend fun getGalleries(
        @Query("page") page: Int
    ): GalleryListResponse

    @GET("api/v2/galleries/popular")
    suspend fun getPopularGalleries(): List<GalleryListItemDto>

    @GET("api/v2/galleries/random")
    suspend fun getRandomGallery(): Map<String, Int>

    @GET("api/v2/galleries/{gallery_id}")
    suspend fun getGalleryDetail(
        @Path("gallery_id") galleryId: Int,
        @Query("include") include: String? = null
    ): GalleryDetailResponse

    @GET("api/v2/galleries/{gallery_id}/related")
    suspend fun getRelatedGalleries(
        @Path("gallery_id") galleryId: Int
    ): RelatedGalleriesResponse

    @GET("api/v2/cdn")
    suspend fun getCdnConfig(): com.example.nhviewer.data.remote.dto.CdnConfigDto
}
