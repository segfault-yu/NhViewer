package com.example.nhviewer.presentation.feature.tagged

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.nhviewer.data.paging.TaggedGalleryPagingSource
import com.example.nhviewer.domain.model.CdnConfig
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.repository.TagRepository
import com.example.nhviewer.domain.usecase.GetCdnConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaggedGalleriesViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val getCdnConfigUseCase: GetCdnConfigUseCase
) : ViewModel() {

    private val _tagId = MutableStateFlow<Int?>(null)

    private val _cdnConfig = MutableStateFlow<CdnConfig?>(getCdnConfigUseCase.cached())
    val cdnConfig: StateFlow<CdnConfig?> = _cdnConfig.asStateFlow()

    val galleries: Flow<PagingData<GalleryListItem>> = _tagId.flatMapLatest { tagId ->
        if (tagId == null) {
            flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(pageSize = 25, prefetchDistance = 5),
                pagingSourceFactory = { TaggedGalleryPagingSource(tagRepository, tagId) }
            ).flow.cachedIn(viewModelScope)
        }
    }

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

    fun setTagId(tagId: Int) {
        _tagId.value = tagId
    }
}
