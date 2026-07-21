package com.example.nhviewer.presentation.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nhviewer.R
import com.example.nhviewer.presentation.common.SearchResultGrid
import com.example.nhviewer.presentation.common.SearchSortBar
import com.example.nhviewer.presentation.common.SearchSuggestionPanel
import com.example.nhviewer.presentation.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val gridBaseWidth by settingsViewModel.gridBaseWidth.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val active by viewModel.active.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val cdnHost = cdnConfig?.primaryImageHost ?: ""

    val searchHistory by viewModel.searchHistory.collectAsState(initial = emptyList())
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val favoritedIds by viewModel.favoritedIds.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearch = {
                    viewModel.search(it)
                    focusManager.clearFocus()
                },
                active = active,
                onActiveChange = { viewModel.onActiveChanged(it) },
                placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                colors = SearchBarDefaults.colors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                SearchSuggestionPanel(
                    searchQuery = searchQuery,
                    searchHistory = searchHistory,
                    autocompleteSuggestions = autocompleteSuggestions,
                    onSearch = { viewModel.search(it) },
                    onDeleteHistory = { viewModel.deleteSearchHistory(it) },
                    onClearAllHistory = { viewModel.clearSearchHistory() }
                )
            }
        }

        if (!active) {
            SearchSortBar(
                sortOption = sortOption,
                onSortOptionChanged = { viewModel.onSortOptionChanged(it) }
            )

            SearchResultGrid(
                searchResults = searchResults,
                cdnHost = cdnHost,
                favoritedIds = favoritedIds,
                onNavigateToDetail = onNavigateToDetail,
                minSize = 340
            )
        }
    }
}
