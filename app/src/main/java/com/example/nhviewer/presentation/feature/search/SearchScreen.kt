package com.example.nhviewer.presentation.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nhviewer.R
import com.example.nhviewer.presentation.common.NhSearchBar
import com.example.nhviewer.presentation.common.SearchQueryBuilder
import com.example.nhviewer.presentation.common.SearchResultGrid
import com.example.nhviewer.presentation.common.SearchSortBar
import com.example.nhviewer.ui.theme.NhMotion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: String? = null,
    viewModel: SearchViewModel = hiltViewModel()
) {
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            viewModel.search(initialQuery)
        }
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val active by viewModel.active.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val cdnHost = cdnConfig?.primaryThumbHost ?: ""

    val searchHistory by viewModel.searchHistory.collectAsState(initial = emptyList())
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val favoritedIds by viewModel.favoritedIds.collectAsState()
    val totalResults by viewModel.totalResults.collectAsState()
    val searchTrigger by viewModel.searchTrigger.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 6.dp)
        ) {
            NhSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearch = {
                    viewModel.search(it)
                    focusManager.clearFocus()
                },
                expanded = active,
                onExpandedChange = { viewModel.onActiveChanged(it) },
                searchHistory = searchHistory,
                autocompleteSuggestions = autocompleteSuggestions,
                onDeleteHistory = { viewModel.deleteSearchHistory(it) },
                onClearAllHistory = { viewModel.clearSearchHistory() },
                placeholder = {
                    Text(
                        text = stringResource(R.string.home_search_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    // 独立搜索页没有底部导航兜底，收起态点这个箭头退出页面；
                    // 展开态（建议面板）时先收起面板而不是直接退出，避免误触返回
                    IconButton(onClick = { if (active) viewModel.onActiveChanged(false) else onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }

        if (!active) {
            if (searchQuery.isNotEmpty()) {
                SearchQueryBuilder(
                    rawQuery = searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onTriggerSearch = { query ->
                        viewModel.search(query)
                        focusManager.clearFocus()
                    }
                )
            }
            AnimatedVisibility(
                visible = searchResults.itemCount > 0,
                enter = fadeIn(animationSpec = NhMotion.Effects.default()),
                exit = fadeOut(animationSpec = NhMotion.Effects.default())
            ) {
                SearchSortBar(
                    sortOption = sortOption,
                    onSortOptionChanged = { viewModel.onSortOptionChanged(it) },
                    totalCount = totalResults
                )
            }

            SearchResultGrid(
                searchResults = searchResults,
                cdnHost = cdnHost,
                favoritedIds = favoritedIds,
                onNavigateToDetail = onNavigateToDetail,
                scrollToTopKey = searchTrigger
            )
        }
    }
}
