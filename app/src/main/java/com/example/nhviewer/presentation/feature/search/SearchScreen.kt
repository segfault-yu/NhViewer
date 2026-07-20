package com.example.nhviewer.presentation.feature.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.example.nhviewer.R
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.presentation.common.EmptyState
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.presentation.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val sortLabelMap = mapOf(
        "date" to stringResource(R.string.sort_date),
        "popular" to stringResource(R.string.sort_popular),
        "popular-today" to stringResource(R.string.sort_popular_today),
        "popular-week" to stringResource(R.string.sort_popular_week),
        "popular-month" to stringResource(R.string.sort_popular_month)
    )

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.home_clear_history_title)) },
            text = { Text(stringResource(R.string.home_clear_history_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSearchHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }

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
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (searchQuery.isBlank()) {
                        if (searchHistory.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_search_history),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(onClick = { showClearHistoryDialog = true }) {
                                        Text(stringResource(R.string.home_search_history_clear), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            items(
                                items = searchHistory,
                                key = { it.query }
                            ) { history ->
                                SwipeToDeleteHistoryItem(
                                    history = history,
                                    onHistoryClick = { query ->
                                        viewModel.search(query)
                                        focusManager.clearFocus()
                                    },
                                    onDelete = { query -> viewModel.deleteSearchHistory(query) },
                                    onLongPress = { showClearHistoryDialog = true }
                                )
                            }
                        }
                    } else {
                        if (autocompleteSuggestions.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.home_search_suggestions),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }

                            items(
                                items = autocompleteSuggestions,
                                key = { it.id }
                            ) { tag ->
                                ListItem(
                                    headlineContent = {
                                        Text(text = tag.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                                    },
                                    supportingContent = {
                                        Text(text = stringResource(R.string.home_search_tag_sub, tag.type, tag.count), style = MaterialTheme.typography.bodySmall)
                                    },
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = "Tag"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.search(tag.name)
                                            focusManager.clearFocus()
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!active) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showSortMenu = true }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sortLabelMap[sortOption] ?: stringResource(R.string.sort_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        sortLabelMap.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.onSortOptionChanged(key)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (searchResults.loadState.refresh is LoadState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else if (searchResults.loadState.refresh is LoadState.Error) {
                    val error = (searchResults.loadState.refresh as LoadState.Error).error
                    ErrorScreen(
                        message = error.localizedMessage ?: stringResource(R.string.home_search_error),
                        onRetry = { searchResults.retry() }
                    )
                } else if (searchResults.itemCount == 0 && searchResults.loadState.refresh is LoadState.NotLoading) {
                    EmptyState(message = stringResource(R.string.home_search_empty))
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = gridBaseWidth.dp),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = searchResults.itemCount,
                            key = { index -> searchResults[index]?.id ?: index }
                        ) { index ->
                            val item = searchResults[index]
                            if (item != null) {
                                GalleryCard(
                                    item = item,
                                    cdnHost = cdnHost,
                                    onClick = { onNavigateToDetail(item.id) },
                                    isFavorited = item.id in favoritedIds,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }

                        if (searchResults.loadState.append is LoadState.Loading) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeToDeleteHistoryItem(
    history: SearchHistory,
    onHistoryClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(history.query)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = history.query,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History"
                )
            },
            trailingContent = {
                IconButton(onClick = { onDelete(history.query) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove single",
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onHistoryClick(history.query) },
                    onLongClick = onLongPress
                )
        )
    }
}
