package com.example.nhviewer.presentation.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.GetGalleryDetailUseCase
import com.example.nhviewer.domain.usecase.GetRelatedGalleriesUseCase
import com.example.nhviewer.domain.usecase.ReadingHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.nhviewer.util.NetworkErrorParser
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getGalleryDetailUseCase: GetGalleryDetailUseCase,
    private val getRelatedGalleriesUseCase: GetRelatedGalleriesUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val readingHistoryUseCase: ReadingHistoryUseCase
) : ViewModel() {

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private val _relatedState = MutableStateFlow<RelatedUiState>(RelatedUiState.Loading)
    val relatedState: StateFlow<RelatedUiState> = _relatedState.asStateFlow()

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(null)
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    private val _readingHistory = MutableStateFlow<ReadingHistory?>(null)
    val readingHistory: StateFlow<ReadingHistory?> = _readingHistory.asStateFlow()

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

            // Fetch local reading history
            val history = readingHistoryUseCase.getReadingHistoryItem(galleryId)
            _readingHistory.value = history

            // Dual stage loading: fetch detail with include=related
            getGalleryDetailUseCase(galleryId, includeRelated = true)
                .onSuccess { detail ->
                    _detailState.value = DetailUiState.Success(detail)
                    
                    // Check if related galleries came with the response
                    if (!detail.related.isNullOrEmpty()) {
                        _relatedState.value = RelatedUiState.Success(detail.related)
                    } else {
                        // Fallback: Fetch related galleries independently
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
