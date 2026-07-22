
package com.example.nhviewer.presentation.feature.blacklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.usecase.AddToBlacklistUseCase
import com.example.nhviewer.domain.usecase.AutocompleteTagsUseCase
import com.example.nhviewer.domain.usecase.GetBlacklistUseCase
import com.example.nhviewer.domain.usecase.RemoveFromBlacklistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlacklistViewModel @Inject constructor(
    private val getBlacklistUseCase: GetBlacklistUseCase,
    private val addToBlacklistUseCase: AddToBlacklistUseCase,
    private val removeFromBlacklistUseCase: RemoveFromBlacklistUseCase,
    private val autocompleteTagsUseCase: AutocompleteTagsUseCase
) : ViewModel() {

    private val _blacklist = MutableStateFlow<List<Tag>>(emptyList())
    val blacklist: StateFlow<List<Tag>> = _blacklist.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Tag>>(emptyList())
    val suggestions: StateFlow<List<Tag>> = _suggestions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadBlacklist()
        setupSuggestionsDebounce()
    }

    fun loadBlacklist() {
        viewModelScope.launch {
            _isLoading.value = true
            getBlacklistUseCase().onSuccess {
                _blacklist.value = it
                _errorMessage.value = null
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "拉取黑名单失败"
            }
            _isLoading.value = false
        }
    }

    fun addTagToBlacklist(tag: Tag) {
        viewModelScope.launch {
            _isLoading.value = true
            addToBlacklistUseCase(tag).onSuccess {
                _searchQuery.value = ""
                _suggestions.value = emptyList()
                loadBlacklist()
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "加入黑名单失败"
            }
            _isLoading.value = false
        }
    }

    fun removeTagFromBlacklist(tagId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            removeFromBlacklistUseCase(tagId).onSuccess {
                loadBlacklist()
            }.onFailure {
                _errorMessage.value = it.localizedMessage ?: "移除黑名单失败"
            }
            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    @OptIn(FlowPreview::class)
    private fun setupSuggestionsDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .filter { it.isNotBlank() }
                .collect { query ->
                    autocompleteTagsUseCase(query).collect { tagList ->
                        val blacklistIds = _blacklist.value.map { it.id }.toSet()
                        _suggestions.value = tagList.filter { tag -> tag.id !in blacklistIds }
                    }
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
