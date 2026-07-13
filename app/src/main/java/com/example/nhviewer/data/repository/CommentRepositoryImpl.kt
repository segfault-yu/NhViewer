package com.example.nhviewer.data.repository

import com.example.nhviewer.data.remote.CommentApi
import com.example.nhviewer.data.remote.dto.PostCommentRequest
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.Comment
import com.example.nhviewer.domain.repository.CommentRepository
import com.example.nhviewer.util.runCatchingCancelable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val api: CommentApi
) : CommentRepository {

    override suspend fun getComments(galleryId: Int): Result<List<Comment>> = runCatchingCancelable {
        api.getComments(galleryId).map { it.toDomain() }
    }

    override suspend fun postComment(
        galleryId: Int,
        content: String,
        powSolution: String,
        captchaToken: String
    ): Result<Comment> = runCatchingCancelable {
        val request = PostCommentRequest(content, powSolution, captchaToken)
        api.postComment(galleryId, request).toDomain()
    }

    override suspend fun deleteComment(commentId: Int): Result<Unit> = runCatchingCancelable {
        val response = api.deleteComment(commentId)
        if (!response.isSuccessful) {
            throw Exception("删除评论失败: ${response.code()}")
        }
    }

    override suspend fun reportComment(commentId: Int): Result<Unit> = runCatchingCancelable {
        val response = api.reportComment(commentId)
        if (!response.isSuccessful) {
            throw Exception("举报评论失败: ${response.code()}")
        }
    }
}
