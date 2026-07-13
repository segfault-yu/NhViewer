package com.example.nhviewer.data.remote

import com.example.nhviewer.data.remote.dto.BlacklistRequest
import com.example.nhviewer.data.remote.dto.TagResponseDto
import retrofit2.Response
import retrofit2.http.*

// 黑名单 API 接口
interface BlacklistApi {
    // 获取已屏蔽的标签列表
    @GET("api/v2/blacklist")
    suspend fun getBlacklist(): List<TagResponseDto>

    // 添加标签至黑名单
    @POST("api/v2/blacklist")
    suspend fun addToBlacklist(
        @Body request: BlacklistRequest
    ): TagResponseDto

    // 从黑名单中移除标签
    @DELETE("api/v2/blacklist/{tag_id}")
    suspend fun removeFromBlacklist(
        @Path("tag_id") tagId: Int
    ): Response<Unit>
}
