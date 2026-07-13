package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.repository.CommentRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(commentId: Int): Result<Unit> {
        return repository.deleteComment(commentId)
    }
}
