package com.example.nhviewer.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.nhviewer.data.paging.GalleryPagingSource
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.repository.GalleryRepository
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.GetPopularGalleriesUseCase
import com.example.nhviewer.domain.usecase.GetRandomGalleryUseCase
import com.example.nhviewer.domain.usecase.ReadingHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.nhviewer.util.NetworkErrorParser
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GalleryRepository,
    private val getPopularGalleriesUseCase: GetPopularGalleriesUseCase,
    private val getRandomGalleryUseCase: GetRandomGalleryUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val readingHistoryUseCase: ReadingHistoryUseCase
) : ViewModel() {

    val latestGalleries: Flow<PagingData<GalleryListItem>> = Pager(
        config = PagingConfig(pageSize = 25, prefetchDistance = 5),
        pagingSourceFactory = { GalleryPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)

    private val _popularGalleriesState = MutableStateFlow<PopularState>(PopularState.Loading)
    val popularGalleriesState: StateFlow<PopularState> = _popularGalleriesState.asStateFlow()

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(null)
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    val readingHistory: Flow<List<ReadingHistory>> = readingHistoryUseCase.getReadingHistory()

    private val _navigationEvent = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvent: SharedFlow<HomeNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadPopularGalleries()
        loadCdnConfig()
    }

    fun loadPopularGalleries() {
        viewModelScope.launch {
            _popularGalleriesState.value = PopularState.Loading
            getPopularGalleriesUseCase().onSuccess {
                _popularGalleriesState.value = PopularState.Success(it)
            }.onFailure { error ->
                _popularGalleriesState.value = PopularState.Error(NetworkErrorParser.parse(error))
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

    fun onRandomClicked() {
        viewModelScope.launch {
            getRandomGalleryUseCase().onSuccess { randomId ->
                _navigationEvent.emit(HomeNavigationEvent.NavigateToDetail(randomId))
            }.onFailure {
                // Emits to navigation or error state
            }
        }
    }

    sealed interface PopularState {
        object Loading : PopularState
        data class Success(val items: List<GalleryListItem>) : PopularState
        data class Error(val message: String) : PopularState
    }

    sealed interface HomeNavigationEvent {
        data class NavigateToDetail(val galleryId: Int) : HomeNavigationEvent
    }
}
