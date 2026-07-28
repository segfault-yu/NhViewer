package com.example.nhviewer.presentation.feature.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.nhviewer.R
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.domain.usecase.CheckIsFavoriteUseCase
import com.example.nhviewer.domain.usecase.GetCachedGalleryDetailUseCase
import com.example.nhviewer.domain.usecase.GetCachedGalleryPreviewUseCase
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.GetGalleryDetailUseCase
import com.example.nhviewer.domain.usecase.GetRelatedGalleriesUseCase
import com.example.nhviewer.domain.usecase.ReadingHistoryUseCase
import com.example.nhviewer.domain.usecase.ToggleFavoriteUseCase
import com.example.nhviewer.presentation.navigation.Route
import com.example.nhviewer.util.NetworkErrorParser
import com.example.nhviewer.util.log.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val getCachedGalleryDetailUseCase: GetCachedGalleryDetailUseCase,
    private val getCachedGalleryPreviewUseCase: GetCachedGalleryPreviewUseCase,
    private val getRelatedGalleriesUseCase: GetRelatedGalleriesUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val readingHistoryUseCase: ReadingHistoryUseCase,
    private val checkIsFavoriteUseCase: CheckIsFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private val _relatedState = MutableStateFlow<RelatedUiState>(RelatedUiState.Loading)
    val relatedState: StateFlow<RelatedUiState> = _relatedState.asStateFlow()

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(getCdnConfigUseCase.cached())
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    private val _readingHistory = MutableStateFlow<ReadingHistory?>(null)
    val readingHistory: StateFlow<ReadingHistory?> = _readingHistory.asStateFlow()

    // 列表快照，供首屏预填与详情封面的占位图复用
    private val _previewItem = MutableStateFlow<GalleryListItem?>(null)
    val previewItem: StateFlow<GalleryListItem?> = _previewItem.asStateFlow()

    // 收藏状态
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DetailUiEvent>()
    val uiEvent: SharedFlow<DetailUiEvent> = _uiEvent.asSharedFlow()

    val authState: StateFlow<AuthState> = userRepository.authState

    private var currentDetail: GalleryDetail? = null
    private var detailJob: Job? = null

    init {
        loadCdnConfig()
        prefillFromSnapshot()
    }

    /**
     * 首帧即用列表快照撑起页面。
     * LaunchedEffect 要等首次组合提交后才跑，只靠它填充会先闪一帧空白骨架。
     */
    private fun prefillFromSnapshot() {
        val galleryId = runCatching {
            savedStateHandle.toRoute<Route.GalleryDetail>().galleryId
        }.getOrNull() ?: return

        val preview = getCachedGalleryPreviewUseCase(galleryId) ?: return
        _previewItem.value = preview
        _detailState.value = DetailUiState.Preview(preview)
    }

    private fun loadCdnConfig() {
        viewModelScope.launch {
            getCdnConfigUseCase().onSuccess {
                _cdnConfig.value = it
            }
        }
    }

    fun loadGalleryDetail(galleryId: Int, forceRefresh: Boolean = false) {
        // 阅读历史与收藏状态并行加载，不阻塞详情首屏
        refreshLocalState(galleryId)

        // 非强制刷新且当前已持有该画廊的完整数据，直接复用
        if (!forceRefresh && currentDetail?.id == galleryId && _detailState.value is DetailUiState.Success) {
            return
        }

        _previewItem.value = getCachedGalleryPreviewUseCase(galleryId)

        if (forceRefresh) {
            _detailState.value = DetailUiState.Loading
            _relatedState.value = RelatedUiState.Loading
            fetchDetail(galleryId, forceRefresh = true, silent = false)
            return
        }

        val cached = getCachedGalleryDetailUseCase(galleryId)
        if (cached != null) {
            applyDetail(cached.detail)
            // 缓存超出新鲜期时后台静默刷新，界面保持旧数据直到新数据返回
            if (System.currentTimeMillis() - cached.cachedAt > STALE_THRESHOLD_MILLIS) {
                fetchDetail(galleryId, forceRefresh = true, silent = true)
            }
            return
        }

        // 无详情缓存时先用列表快照撑起首屏，避免整页空白
        _detailState.value = _previewItem.value
            ?.let { DetailUiState.Preview(it) }
            ?: DetailUiState.Loading
        _relatedState.value = RelatedUiState.Loading
        fetchDetail(galleryId, forceRefresh = false, silent = false)
    }

    private fun refreshLocalState(galleryId: Int) {
        viewModelScope.launch {
            _readingHistory.value = readingHistoryUseCase.getReadingHistoryItem(galleryId)
        }
        viewModelScope.launch {
            checkIsFavoriteUseCase(galleryId).onSuccess {
                _isFavorite.value = it
            }
        }
    }

    private fun fetchDetail(galleryId: Int, forceRefresh: Boolean, silent: Boolean) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            getGalleryDetailUseCase(galleryId, includeRelated = true, forceRefresh = forceRefresh)
                .onSuccess { detail ->
                    applyDetail(detail)
                }
                .onFailure { error ->
                    AppLogger.w("Detail", "画廊 $galleryId 详情加载失败 (silent=$silent)")
                    // 静默刷新失败时保留已展示的旧数据，不打断阅读
                    if (silent) return@onFailure
                    val message = NetworkErrorParser.parse(error)
                    _detailState.value = DetailUiState.Error(message)
                    _relatedState.value = RelatedUiState.Error(message)
                }
        }
    }

    private fun applyDetail(detail: GalleryDetail) {
        currentDetail = detail
        _detailState.value = DetailUiState.Success(detail)

        val related = detail.related
        if (!related.isNullOrEmpty()) {
            _relatedState.value = RelatedUiState.Success(related)
        } else if (_relatedState.value !is RelatedUiState.Success) {
            fetchRelatedIndependently(detail.id)
        }
    }

    private fun fetchRelatedIndependently(galleryId: Int) {
        viewModelScope.launch {
            getRelatedGalleriesUseCase(galleryId)
                .onSuccess { list ->
                    _relatedState.value = RelatedUiState.Success(list)
                }
                .onFailure { error ->
                    AppLogger.w("Detail", "画廊 $galleryId 相关推荐加载失败")
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
                AppLogger.w("Detail", "画廊 ${detail.id} 收藏切换失败，回滚为 $previousState")
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

        /** 列表快照预填的首屏，详情数据到达前先展示已知信息 */
        data class Preview(val item: GalleryListItem) : DetailUiState

        data class Success(val detail: GalleryDetail) : DetailUiState
        data class Error(val message: String) : DetailUiState
    }

    sealed interface RelatedUiState {
        object Loading : RelatedUiState
        data class Success(val list: List<GalleryListItem>) : RelatedUiState
        data class Error(val message: String) : RelatedUiState
    }

    private companion object {
        /** 内存缓存的新鲜期，超过则命中后触发一次后台静默刷新 */
        const val STALE_THRESHOLD_MILLIS = 5 * 60 * 1000L
    }
}
