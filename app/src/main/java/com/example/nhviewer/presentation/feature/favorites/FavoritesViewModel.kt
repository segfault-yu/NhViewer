package com.example.nhviewer.presentation.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.nhviewer.data.paging.FavoritesPagingSource
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.BlacklistRepository
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.GetFavoritesUseCase
import com.example.nhviewer.domain.usecase.GetRandomFavoriteUseCase
import com.example.nhviewer.domain.usecase.SyncFavoritesUseCase
import com.example.nhviewer.domain.repository.UserRepository
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.util.log.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getRandomFavoriteUseCase: GetRandomFavoriteUseCase,
    private val syncFavoritesUseCase: SyncFavoritesUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val blacklistRepository: BlacklistRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<FavoritesUiEvent>()
    val uiEvent: SharedFlow<FavoritesUiEvent> = _uiEvent.asSharedFlow()

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(null)
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    val authState: StateFlow<AuthState> = userRepository.authState

    val favoritesPagingData: Flow<PagingData<GalleryListItem>> = Pager(
        config = PagingConfig(pageSize = 25, prefetchDistance = 5),
        pagingSourceFactory = { FavoritesPagingSource(getFavoritesUseCase) }
    ).flow
        .cachedIn(viewModelScope)
        .combine(blacklistRepository.blacklistedTagIds) { pagingData, blacklistedIds ->
            pagingData.filter { item ->
                !item.tagIds.any { it in blacklistedIds }
            }
        }

    init {
        loadCdnConfig()
        viewModelScope.launch {
            userRepository.authState.collect { state ->
                if (state is AuthState.LoggedIn) {
                    syncFavoritesUseCase()
                }
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

    fun playRandomFavorite() {
        viewModelScope.launch {
            getRandomFavoriteUseCase()
                .onSuccess {
                    _uiEvent.emit(FavoritesUiEvent.NavigateToDetail(it.id))
                }
                .onFailure {
                    AppLogger.w("Favorites", "随机收藏获取失败")
                    _uiEvent.emit(FavoritesUiEvent.ShowError(it.localizedMessage ?: "未能获取随机收藏"))
                }
        }
    }

    sealed interface FavoritesUiEvent {
        data class NavigateToDetail(val galleryId: Int) : FavoritesUiEvent
        data class ShowError(val message: String) : FavoritesUiEvent
    }
}
