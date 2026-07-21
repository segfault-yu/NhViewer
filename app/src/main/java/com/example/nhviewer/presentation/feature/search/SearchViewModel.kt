package com.example.nhviewer.presentation.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.nhviewer.data.paging.SearchPagingSource
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.data.local.SettingsManager
import com.example.nhviewer.domain.repository.BlacklistRepository
import com.example.nhviewer.domain.repository.FavoriteRepository
import com.example.nhviewer.domain.repository.SearchRepository
import com.example.nhviewer.domain.usecase.AutocompleteTagsUseCase
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import com.example.nhviewer.domain.usecase.SearchHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val autocompleteTagsUseCase: AutocompleteTagsUseCase,
    private val getCdnConfigUseCase: GetCdnConfigUseCase,
    private val searchHistoryUseCase: SearchHistoryUseCase,
    private val blacklistRepository: BlacklistRepository,
    private val favoriteRepository: FavoriteRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active.asStateFlow()

    private val _sortOption = MutableStateFlow("date")
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(null)
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    val searchHistory: Flow<List<SearchHistory>> = searchHistoryUseCase.getSearchHistory()

    private val _searchTrigger = MutableStateFlow<Pair<String, String>?>(null)

    val searchResults: Flow<PagingData<GalleryListItem>> = _searchTrigger
        .flatMapLatest { trigger ->
            if (trigger == null || trigger.first.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                Pager(
                    config = PagingConfig(pageSize = 25, prefetchDistance = 5),
                    pagingSourceFactory = { SearchPagingSource(searchRepository, trigger.first, trigger.second) }
                ).flow.cachedIn(viewModelScope)
            }
        }
        .combine(blacklistRepository.blacklistedTagIds) { pagingData, blacklistedIds ->
            pagingData.filter { item ->
                !item.tagIds.any { it in blacklistedIds }
            }
        }

    val favoritedIds: StateFlow<Set<Int>> = favoriteRepository.favoritesFlow
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val autocompleteSuggestions: StateFlow<List<Tag>> = _searchQuery
        .debounce(600)
        .combine(settingsManager.tagLanguage) { query, tagLanguage ->
            query to tagLanguage
        }
        .flatMapLatest { (query, tagLanguage) ->
            if (query.isBlank()) {
                flowOf(Result.success(emptyList<Tag>()))
            } else {
                flow { emit(autocompleteTagsUseCase(query, null, tagLanguage)) }
            }
        }
        .map { result -> result.getOrElse { emptyList() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onActiveChanged(active: Boolean) {
        _active.value = active
    }

    fun onSortOptionChanged(sort: String) {
        _sortOption.value = sort
        val query = _searchTrigger.value?.first
        if (!query.isNullOrBlank()) {
            _searchQuery.value = query
            _searchTrigger.value = Pair(query, sort)
        }
    }

    fun search(query: String) {
        if (query.isNotBlank()) {
            _active.value = false
            _searchQuery.value = query
            _searchTrigger.value = Pair(query, _sortOption.value)
            viewModelScope.launch {
                searchHistoryUseCase.addSearchHistory(query)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryUseCase.clearSearchHistory()
        }
    }

    fun deleteSearchHistory(query: String) {
        viewModelScope.launch {
            searchHistoryUseCase.removeSearchHistory(query)
        }
    }
}
