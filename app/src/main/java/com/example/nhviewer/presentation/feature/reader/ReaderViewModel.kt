package com.example.nhviewer.presentation.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.GetGalleryDetailUseCase
import com.example.nhviewer.domain.usecase.ReadingHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import com.example.nhviewer.util.NetworkErrorParser
import com.example.nhviewer.util.log.AppLogger
import javax.inject.Inject
import com.example.nhviewer.data.local.SettingsManager

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getGalleryDetailUseCase: GetGalleryDetailUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val readingHistoryUseCase: ReadingHistoryUseCase,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _detailState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val detailState: StateFlow<ReaderUiState> = _detailState.asStateFlow()

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(null)
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _isScrollMode = MutableStateFlow(true) // Default is vertical scroll
    val isScrollMode: StateFlow<Boolean> = _isScrollMode.asStateFlow()

    private var currentDetail: GalleryDetail? = null
    private val _pageHistoryFlow = MutableSharedFlow<Pair<Int, Int>>(extraBufferCapacity = 1)

    init {
        loadCdnConfig()
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            _pageHistoryFlow
                .debounce(1000)
                .collect { (galleryId, page) ->
                    saveHistoryDirectly(galleryId, page)
                }
        }
        viewModelScope.launch {
            settingsManager.readerDirection.collect { direction ->
                _isScrollMode.value = (direction == "vertical")
            }
        }
    }

    private fun loadCdnConfig() {
        viewModelScope.launch {
            getCdnConfigUseCase().onSuccess {
                _cdnConfig.value = it
            }
        }
    }

    fun loadGallery(galleryId: Int, startPage: Int) {
        if (currentDetail?.id == galleryId) {
            if (_currentPage.value != startPage) {
                _currentPage.value = startPage
            }
            return // Avoid reload on rotation
        }
        
        viewModelScope.launch {
            _detailState.value = ReaderUiState.Loading
            _currentPage.value = startPage

            // 并发拉取 cdnConfig 和 galleryDetail，确保 UI 收到 Success 时 CDN config 已就绪，根除域名闪烁
            val cdnDeferred = async {
                if (_cdnConfig.value == null) {
                    getCdnConfigUseCase().getOrNull()
                } else {
                    _cdnConfig.value
                }
            }
            val detailDeferred = async {
                getGalleryDetailUseCase(galleryId, includeRelated = false)
            }

            val cdn = cdnDeferred.await()
            if (cdn != null) {
                _cdnConfig.value = cdn
            }

            detailDeferred.await()
                .onSuccess { detail ->
                    currentDetail = detail
                    _detailState.value = ReaderUiState.Success(detail)

                    // Record initial history entry
                    saveHistoryDirectly(galleryId, startPage)
                }
                .onFailure { error ->
                    AppLogger.w("Reader", "画廊 $galleryId 阅读详情加载失败")
                    _detailState.value = ReaderUiState.Error(NetworkErrorParser.parse(error))
                }
        }
    }

    fun onPageChanged(galleryId: Int, page: Int) {
        if (page != _currentPage.value) {
            _currentPage.value = page
            saveHistory(galleryId, page)
        }
    }

    fun toggleReadingMode() {
        _isScrollMode.value = !_isScrollMode.value
    }

    private fun saveHistory(galleryId: Int, page: Int) {
        viewModelScope.launch {
            _pageHistoryFlow.emit(Pair(galleryId, page))
        }
    }

    private fun saveHistoryDirectly(galleryId: Int, page: Int) {
        val detail = currentDetail ?: return
        viewModelScope.launch {
            val history = ReadingHistory(
                galleryId = galleryId,
                mediaId = detail.mediaId,
                title = detail.prettyTitle ?: detail.englishTitle,
                lastReadPage = page,
                totalPages = detail.numPages,
                timestamp = System.currentTimeMillis()
            )
            readingHistoryUseCase.saveReadingHistory(history)
        }
    }

    fun flushFinalHistory(galleryId: Int) {
        saveHistoryDirectly(galleryId, _currentPage.value)
    }

    sealed interface ReaderUiState {
        object Loading : ReaderUiState
        data class Success(val detail: GalleryDetail) : ReaderUiState
        data class Error(val message: String) : ReaderUiState
    }
}
