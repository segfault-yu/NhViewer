package com.example.nhviewer.presentation.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.nhviewer.presentation.common.SearchQueryBuilder
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
    val totalResults by viewModel.totalResults.collectAsState()

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
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearch = {
                    viewModel.search(it)
                    focusManager.clearFocus()
                },
                active = active,
                onActiveChange = { viewModel.onActiveChanged(it) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.home_search_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
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
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    SearchQueryBuilder(
                        rawQuery = searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onTriggerSearch = { query ->
                            viewModel.search(query)
                            focusManager.clearFocus()
                        }
                    )
                }
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
            if (searchQuery.isNotEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(250)),
                    exit = fadeOut(animationSpec = tween(250))
                ) {
                    SearchQueryBuilder(
                        rawQuery = searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onTriggerSearch = { query ->
                            viewModel.search(query)
                            focusManager.clearFocus()
                        }
                    )
                }
            }
            AnimatedVisibility(
                visible = searchResults.itemCount > 0,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250))
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
                minSize = gridBaseWidth
            )
        }
    }
}
