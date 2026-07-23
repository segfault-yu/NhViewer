package com.example.nhviewer.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.nhviewer.data.paging.GalleryPagingSource
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.repository.GalleryRepository
import com.example.nhviewer.domain.repository.BlacklistRepository
import com.example.nhviewer.domain.repository.FavoriteRepository
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.nhviewer.util.NetworkErrorParser
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GalleryRepository,
    private val getPopularGalleriesUseCase: GetPopularGalleriesUseCase,
    private val getRandomGalleryUseCase: GetRandomGalleryUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val readingHistoryUseCase: ReadingHistoryUseCase,
    private val blacklistRepository: BlacklistRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val latestGalleries: Flow<PagingData<GalleryListItem>> = Pager(
        config = PagingConfig(pageSize = 25, prefetchDistance = 5),
        pagingSourceFactory = { GalleryPagingSource(repository) }
    ).flow
        .cachedIn(viewModelScope)
        .combine(blacklistRepository.blacklistedTagIds) { pagingData, blacklistedIds ->
            pagingData.filter { item ->
                !item.tagIds.any { it in blacklistedIds }
            }
        }

    private val _popularGalleriesState = MutableStateFlow<PopularState>(PopularState.Loading)

    val popularGalleriesState: StateFlow<PopularState> = _popularGalleriesState
        .combine(blacklistRepository.blacklistedTagIds) { state, blacklistedIds ->
            when (state) {
                is PopularState.Success -> {
                    PopularState.Success(state.items.filter { item ->
                        !item.tagIds.any { it in blacklistedIds }
                    })
                }
                else -> state
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PopularState.Loading)

    val favoritedIds: StateFlow<Set<Int>> = favoriteRepository.favoritesFlow
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(null)
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    val readingHistory: Flow<List<ReadingHistory>> = readingHistoryUseCase.getReadingHistory()

    private val _navigationEvent = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvent: SharedFlow<HomeNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadPopularGalleries()
        loadCdnConfig()
    }

    fun loadPopularGalleries(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _popularGalleriesState.value = PopularState.Loading
            getPopularGalleriesUseCase(forceRefresh)
                .onSuccess { list ->
                    _popularGalleriesState.value = PopularState.Success(list)
                }
                .onFailure { error ->
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

    fun playRandom() {
        viewModelScope.launch {
            getRandomGalleryUseCase()
                .onSuccess { id ->
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToDetail(id))
                }
                .onFailure {
                    // Handle random fetch failure
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
