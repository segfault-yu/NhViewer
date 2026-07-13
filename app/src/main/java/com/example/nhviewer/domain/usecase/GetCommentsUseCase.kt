package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Comment
import com.example.nhviewer.domain.repository.CommentRepository
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(galleryId: Int): Result<List<Comment>> {
        return repository.getComments(galleryId)
    }
}
