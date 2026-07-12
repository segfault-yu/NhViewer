package com.example.nhviewer.presentation.feature.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.nhviewer.data.paging.TagPagingSource
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _currentType = MutableStateFlow("tag")
    val currentType: StateFlow<String> = _currentType.asStateFlow()

    private val _sortOption = MutableStateFlow("popular") // popular or name
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()

    val tagsFlow: Flow<PagingData<Tag>> = combine(_currentType, _sortOption) { type, sort ->
        Pair(type, sort)
    }.flatMapLatest { (type, sort) ->
        Pager(
            config = PagingConfig(pageSize = 25, prefetchDistance = 5),
            pagingSourceFactory = { TagPagingSource(tagRepository, type, sort) }
        ).flow.cachedIn(viewModelScope)
    }

    fun selectTagType(type: String) {
        _currentType.value = type
    }

    fun setSortOption(sort: String) {
        _sortOption.value = sort
    }
}
