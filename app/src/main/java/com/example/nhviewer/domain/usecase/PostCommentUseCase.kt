package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Comment
import com.example.nhviewer.domain.repository.CommentRepository
import javax.inject.Inject

class PostCommentUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(
        galleryId: Int,
        content: String,
        powSolution: String,
        captchaToken: String
    ): Result<Comment> {
        return repository.postComment(galleryId, content, powSolution, captchaToken)
    }
}
