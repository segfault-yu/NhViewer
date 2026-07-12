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
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.presentation.common.EmptyState
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val active by viewModel.active.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val cdnHost = cdnConfig?.primaryImageHost ?: ""

    val searchHistory by viewModel.searchHistory.collectAsState(initial = emptyList())
    val autocompleteSuggestions by viewModel.autocompleteSuggestions.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val sortLabelMap = mapOf(
        "date" to "最新时间",
        "popular" to "最热门",
        "popular-today" to "今日热门",
        "popular-week" to "本周热门",
        "popular-month" to "本月热门"
    )

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("清空历史记录") },
            text = { Text("确定要清除所有的搜索历史记录吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Material 3 SearchBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onQueryChange(it) },
                onSearch = {
                    viewModel.onSearch(it)
                    focusManager.clearFocus()
                },
                active = active,
                onActiveChange = { viewModel.onActiveChange(it) },
                placeholder = { Text("搜索画廊 (例如 pages:>10)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (searchQuery.isBlank()) {
                        // Show Search History Section
                        if (searchHistory.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "历史记录",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(onClick = { showClearHistoryDialog = true }) {
                                        Text("清空全部", fontSize = 12.sp)
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
                                        viewModel.onSearch(query)
                                        focusManager.clearFocus()
                                    },
                                    onDelete = { query -> viewModel.deleteHistory(query) },
                                    onLongPress = { showClearHistoryDialog = true }
                                )
                            }
                        }
                    } else {
                        // Show Autocomplete Suggestions Section
                        if (autocompleteSuggestions.isNotEmpty()) {
                            item {
                                Text(
                                    text = "搜索建议",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
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
                                        Text(text = tag.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    supportingContent = {
                                        Text(text = "类型: ${tag.type}  (${tag.count} 个画廊)")
                                    },
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = "Tag",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onSearch(tag.name)
                                            focusManager.clearFocus()
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Results when search bar is not active
        if (!active) {
            // Sort Header Row
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
                            text = sortLabelMap[sortOption] ?: "排序",
                            fontSize = 13.sp,
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
                                    viewModel.onSortChange(key)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Results grid
            Box(modifier = Modifier.fillMaxSize()) {
                if (searchResults.loadState.refresh is LoadState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else if (searchResults.loadState.refresh is LoadState.Error) {
                    val error = (searchResults.loadState.refresh as LoadState.Error).error
                    ErrorScreen(
                        message = error.localizedMessage ?: "搜索发生错误",
                        onRetry = { searchResults.retry() }
                    )
                } else if (searchResults.itemCount == 0 && searchResults.loadState.refresh is LoadState.NotLoading) {
                    EmptyState(message = "未找到匹配的画廊")
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
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
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
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
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            trailingContent = {
                IconButton(onClick = { onDelete(history.query) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove single",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
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
