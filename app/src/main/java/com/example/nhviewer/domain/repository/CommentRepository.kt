package com.example.nhviewer.domain.repository

import com.example.nhviewer.domain.model.Comment

// 评论仓库接口
interface CommentRepository {
    // 获取画廊评论列表
    suspend fun getComments(galleryId: Int): Result<List<Comment>>
    
    // 发表画廊评论
    suspend fun postComment(
        galleryId: Int,
        content: String,
        powSolution: String,
        captchaToken: String
    ): Result<Comment>

    // 删除指定评论
    suspend fun deleteComment(commentId: Int): Result<Unit>
    
    // 举报指定评论
    suspend fun reportComment(commentId: Int): Result<Unit>
}
