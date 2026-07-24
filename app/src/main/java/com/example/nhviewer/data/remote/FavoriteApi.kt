package com.example.nhviewer.data.remote

import com.example.nhviewer.data.remote.dto.FavoriteCheckResponse
import com.example.nhviewer.data.remote.dto.GalleryListItemDto
import com.example.nhviewer.data.remote.dto.GalleryListResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// 收藏夹 API 接口
interface FavoriteApi {
    // 分页获取收藏画廊列表
    @GET("api/v2/favorites")
    suspend fun getFavorites(
        @Query("page") page: Int
    ): GalleryListResponse

    // 从收藏夹中随机获取一本画廊
    @GET("api/v2/favorites/random")
    suspend fun getRandomFavorite(): GalleryListItemDto

    // 检查特定画廊是否在收藏夹中
    @GET("api/v2/galleries/{gallery_id}/favorite")
    suspend fun checkIsFavorite(
        @Path("gallery_id") galleryId: Int
    ): FavoriteCheckResponse

    // 添加画廊至收藏夹
    @POST("api/v2/galleries/{gallery_id}/favorite")
    suspend fun addFavorite(
        @Path("gallery_id") galleryId: Int
    ): Response<Unit>

    // 从收藏夹中移出画廊
    @DELETE("api/v2/galleries/{gallery_id}/favorite")
    suspend fun removeFavorite(
        @Path("gallery_id") galleryId: Int
    ): Response<Unit>
}
