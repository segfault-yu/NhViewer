package com.example.nhviewer.presentation.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.model.Comment
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.GetGalleryDetailUseCase
import com.example.nhviewer.domain.usecase.GetRelatedGalleriesUseCase
import com.example.nhviewer.domain.usecase.ReadingHistoryUseCase
import com.example.nhviewer.domain.usecase.CheckIsFavoriteUseCase
import com.example.nhviewer.domain.usecase.ToggleFavoriteUseCase
import com.example.nhviewer.domain.usecase.GetCommentsUseCase
import com.example.nhviewer.domain.usecase.PostCommentUseCase
import com.example.nhviewer.domain.usecase.DeleteCommentUseCase
import com.example.nhviewer.domain.usecase.ReportCommentUseCase
import com.example.nhviewer.domain.usecase.GetPowChallengeUseCase
import com.example.nhviewer.domain.usecase.GetCaptchaConfigUseCase
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.util.PowSolver
import com.example.nhviewer.util.NetworkErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getGalleryDetailUseCase: GetGalleryDetailUseCase,
    private val getRelatedGalleriesUseCase: GetRelatedGalleriesUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val readingHistoryUseCase: ReadingHistoryUseCase,
    private val checkIsFavoriteUseCase: CheckIsFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val getPowChallengeUseCase: GetPowChallengeUseCase,
    private val getCaptchaConfigUseCase: GetCaptchaConfigUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private val _relatedState = MutableStateFlow<RelatedUiState>(RelatedUiState.Loading)
    val relatedState: StateFlow<RelatedUiState> = _relatedState.asStateFlow()

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(null)
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    private val _readingHistory = MutableStateFlow<ReadingHistory?>(null)
    val readingHistory: StateFlow<ReadingHistory?> = _readingHistory.asStateFlow()

    // Favorites state
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Comments states
    private val _commentsState = MutableStateFlow<CommentsUiState>(CommentsUiState.Loading)
    val commentsState: StateFlow<CommentsUiState> = _commentsState.asStateFlow()

    private val _powStatus = MutableStateFlow("Idle")
    val powStatus: StateFlow<String> = _powStatus.asStateFlow()

    private val _captchaSiteKey = MutableStateFlow<String?>(null)
    val captchaSiteKey: StateFlow<String?> = _captchaSiteKey.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DetailUiEvent>()
    val uiEvent: SharedFlow<DetailUiEvent> = _uiEvent.asSharedFlow()

    val authState: StateFlow<AuthState> = userRepository.authState

    private var currentDetail: GalleryDetail? = null
    private var pendingCommentText = ""
    private var pendingCommentSolution = ""

    init {
        loadCdnConfig()
    }

    private fun loadCdnConfig() {
        viewModelScope.launch {
            getCdnConfigUseCase().onSuccess {
                _cdnConfig.value = it
            }
        }
    }

    fun loadGalleryDetail(galleryId: Int) {
        viewModelScope.launch {
            _detailState.value = DetailUiState.Loading
            _relatedState.value = RelatedUiState.Loading
            _commentsState.value = CommentsUiState.Loading

            // Fetch local reading history
            val history = readingHistoryUseCase.getReadingHistoryItem(galleryId)
            _readingHistory.value = history

            // Check favorite status
            checkIsFavoriteUseCase(galleryId).onSuccess {
                _isFavorite.value = it
            }

            // Fetch comments
            fetchComments(galleryId)

            // Dual stage loading: fetch detail with include=related
            getGalleryDetailUseCase(galleryId, includeRelated = true)
                .onSuccess { detail ->
                    currentDetail = detail
                    _detailState.value = DetailUiState.Success(detail)
                    
                    if (!detail.related.isNullOrEmpty()) {
                        _relatedState.value = RelatedUiState.Success(detail.related)
                    } else {
                        fetchRelatedIndependently(galleryId)
                    }
                }
                .onFailure { error ->
                    _detailState.value = DetailUiState.Error(NetworkErrorParser.parse(error))
                    _relatedState.value = RelatedUiState.Error(NetworkErrorParser.parse(error))
                }
        }
    }

    private fun fetchRelatedIndependently(galleryId: Int) {
        viewModelScope.launch {
            getRelatedGalleriesUseCase(galleryId)
                .onSuccess { list ->
                    _relatedState.value = RelatedUiState.Success(list)
                }
                .onFailure { error ->
                    _relatedState.value = RelatedUiState.Error(NetworkErrorParser.parse(error))
                }
        }
    }

    private fun fetchComments(galleryId: Int) {
        viewModelScope.launch {
            getCommentsUseCase(galleryId)
                .onSuccess {
                    _commentsState.value = CommentsUiState.Success(it)
                }
                .onFailure {
                    _commentsState.value = CommentsUiState.Error(NetworkErrorParser.parse(it))
                }
        }
    }

    // Toggle favorite (with Optimistic Update)
    fun toggleFavorite() {
        if (userRepository.authState.value is AuthState.LoggedOut) {
            emitMessage("请先登录后使用收藏功能")
            return
        }
        val detail = currentDetail ?: return
        val previousState = _isFavorite.value
        val newState = !previousState
        
        // Optimistic update
        _isFavorite.value = newState
        
        // Optimistically modify local detail count in state
        val updatedDetail = detail.copy(
            numFavorites = if (newState) detail.numFavorites + 1 else maxOf(0, detail.numFavorites - 1)
        )
        currentDetail = updatedDetail
        _detailState.value = DetailUiState.Success(updatedDetail)

        viewModelScope.launch {
            val listItem = GalleryListItem(
                id = detail.id,
                mediaId = detail.mediaId,
                englishTitle = detail.englishTitle,
                japaneseTitle = detail.japaneseTitle,
                thumbnail = detail.thumbnailPath,
                thumbnailWidth = detail.thumbnailWidth,
                thumbnailHeight = detail.thumbnailHeight,
                numPages = detail.numPages,
                numFavorites = updatedDetail.numFavorites,
                tagIds = detail.tags.map { it.id },
                blacklisted = false
            )
            toggleFavoriteUseCase(listItem, newState).onFailure { error ->
                // Rollback
                _isFavorite.value = previousState
                currentDetail = detail
                _detailState.value = DetailUiState.Success(detail)
                _uiEvent.emit(DetailUiEvent.ShowMessage(NetworkErrorParser.parse(error)))
            }
        }
    }

    // Comments posting safety barrier
    fun startPostComment(content: String) {
        if (content.isBlank()) {
            emitMessage("评论内容不能为空")
            return
        }
        val detail = currentDetail ?: return
        pendingCommentText = content
        
        viewModelScope.launch {
            _powStatus.value = "获取 PoW 挑战配置..."
            getPowChallengeUseCase(action = "comment").onSuccess { powDto ->
                _powStatus.value = "解算 PoW 工作量中..."
                val nonce = PowSolver.solve(powDto.challenge, powDto.difficulty)
                pendingCommentSolution = nonce
                
                _powStatus.value = "获取人机验证机制..."
                getCaptchaConfigUseCase().onSuccess { captchaDto ->
                    _powStatus.value = "等待进行人机验证..."
                    _captchaSiteKey.value = captchaDto.siteKey
                }.onFailure {
                    resetPostStates()
                    emitMessage("获取验证配置失败: ${it.localizedMessage}")
                }
            }.onFailure {
                resetPostStates()
                emitMessage("获取 PoW 挑战失败: ${it.localizedMessage}")
            }
        }
    }

    fun onCaptchaSuccess(captchaToken: String) {
        _captchaSiteKey.value = null
        _powStatus.value = "提交评论中..."
        val detail = currentDetail ?: return
        
        viewModelScope.launch {
            postCommentUseCase(detail.id, pendingCommentText, pendingCommentSolution, captchaToken)
                .onSuccess {
                    resetPostStates()
                    _uiEvent.emit(DetailUiEvent.CommentPostedSuccess)
                    fetchComments(detail.id)
                }
                .onFailure {
                    resetPostStates()
                    emitMessage(NetworkErrorParser.parse(it))
                }
        }
    }

    fun cancelCaptcha() {
        resetPostStates()
    }

    private fun resetPostStates() {
        _captchaSiteKey.value = null
        _powStatus.value = "Idle"
        pendingCommentText = ""
        pendingCommentSolution = ""
    }

    fun deleteComment(commentId: Int) {
        val detail = currentDetail ?: return
        viewModelScope.launch {
            deleteCommentUseCase(commentId)
                .onSuccess {
                    fetchComments(detail.id)
                }
                .onFailure {
                    emitMessage("删除评论失败: ${it.localizedMessage}")
                }
        }
    }

    fun reportComment(commentId: Int) {
        viewModelScope.launch {
            reportCommentUseCase(commentId)
                .onSuccess {
                    _uiEvent.emit(DetailUiEvent.ShowMessage("已成功举报该评论"))
                }
                .onFailure {
                    emitMessage("举报评论失败: ${it.localizedMessage}")
                }
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _uiEvent.emit(DetailUiEvent.ShowMessage(msg))
        }
    }

    sealed interface DetailUiEvent {
        data object CommentPostedSuccess : DetailUiEvent
        data class ShowMessage(val message: String) : DetailUiEvent
    }

    sealed interface DetailUiState {
        object Loading : DetailUiState
        data class Success(val detail: GalleryDetail) : DetailUiState
        data class Error(val message: String) : DetailUiState
    }

    sealed interface RelatedUiState {
        object Loading : RelatedUiState
        data class Success(val list: List<GalleryListItem>) : RelatedUiState
        data class Error(val message: String) : RelatedUiState
    }

    sealed interface CommentsUiState {
        object Loading : CommentsUiState
        data class Success(val list: List<Comment>) : CommentsUiState
        data class Error(val message: String) : CommentsUiState
    }
}
