package com.example.nhviewer.presentation.feature.detail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.R
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.domain.usecase.CheckIsFavoriteUseCase
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.GetGalleryDetailUseCase
import com.example.nhviewer.domain.usecase.GetRelatedGalleriesUseCase
import com.example.nhviewer.domain.usecase.ReadingHistoryUseCase
import com.example.nhviewer.domain.usecase.ToggleFavoriteUseCase
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

    // 收藏状态
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DetailUiEvent>()
    val uiEvent: SharedFlow<DetailUiEvent> = _uiEvent.asSharedFlow()

    val authState: StateFlow<AuthState> = userRepository.authState

    private var currentDetail: GalleryDetail? = null

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

    fun loadGalleryDetail(galleryId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // 始终刷新本地阅读历史与收藏状态
            val history = readingHistoryUseCase.getReadingHistoryItem(galleryId)
            _readingHistory.value = history

            checkIsFavoriteUseCase(galleryId).onSuccess {
                _isFavorite.value = it
            }

            // 若非强刷新且数据已成功加载，拦截重复 API 网络请求
            if (!forceRefresh && currentDetail?.id == galleryId && _detailState.value is DetailUiState.Success) {
                return@launch
            }

            _detailState.value = DetailUiState.Loading
            _relatedState.value = RelatedUiState.Loading

            // 阶段加载：获取画廊详情（包含相关推荐）
            getGalleryDetailUseCase(galleryId, includeRelated = true, forceRefresh = forceRefresh)
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

    // 切换收藏状态 (带乐观更新)
    fun toggleFavorite() {
        if (userRepository.authState.value is AuthState.LoggedOut) {
            emitMessageRes(R.string.detail_login_required_favorite)
            return
        }
        val detail = currentDetail ?: return
        val previousState = _isFavorite.value
        val newState = !previousState

        // 乐观更新 UI 状态
        _isFavorite.value = newState

        // 乐观修改本地详情收藏数
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
                // 请求失败回滚状态
                _isFavorite.value = previousState
                currentDetail = detail
                _detailState.value = DetailUiState.Success(detail)
                _uiEvent.emit(DetailUiEvent.ShowMessage(NetworkErrorParser.parse(error)))
            }
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _uiEvent.emit(DetailUiEvent.ShowMessage(msg))
        }
    }

    private fun emitMessageRes(@StringRes resId: Int) {
        viewModelScope.launch {
            _uiEvent.emit(DetailUiEvent.ShowMessageRes(resId))
        }
    }

    sealed interface DetailUiEvent {
        data class ShowMessage(val message: String) : DetailUiEvent
        data class ShowMessageRes(@StringRes val resId: Int) : DetailUiEvent
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
}
