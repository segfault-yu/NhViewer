package com.example.nhviewer.data.remote

import com.example.nhviewer.data.remote.dto.CommentDto
import com.example.nhviewer.data.remote.dto.CommentListResponse
import com.example.nhviewer.data.remote.dto.PostCommentRequest
import retrofit2.Response
import retrofit2.http.*

// 评论 API 接口
interface CommentApi {
    // 获取画廊评论列表
    @GET("api/v2/galleries/{gallery_id}/comments")
    suspend fun getComments(
        @Path("gallery_id") galleryId: Int
    ): CommentListResponse

    // 对画廊发表评论
    @POST("api/v2/galleries/{gallery_id}/comments")
    suspend fun postComment(
        @Path("gallery_id") galleryId: Int,
        @Body request: PostCommentRequest
    ): CommentDto

    // 删除评论
    @DELETE("api/v2/comments/{comment_id}")
    suspend fun deleteComment(
        @Path("comment_id") commentId: Int
    ): Response<Unit>

    // 举报评论
    @POST("api/v2/comments/{comment_id}/report")
    suspend fun reportComment(
        @Path("comment_id") commentId: Int
    ): Response<Unit>
}
