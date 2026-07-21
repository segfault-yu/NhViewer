package com.example.nhviewer.presentation.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.nhviewer.R
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.LocalTagLanguage
import com.example.nhviewer.util.i18n.TagTranslationProvider

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

@Composable
fun SearchSuggestionPanel(
    searchQuery: String,
    searchHistory: List<SearchHistory>,
    autocompleteSuggestions: List<Tag>,
    onSearch: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier,
    tagLanguage: String = LocalTagLanguage.current,
    tagDisplayMode: String = LocalTagDisplayMode.current
) {
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.home_clear_history_title)) },
            text = { Text(stringResource(R.string.home_clear_history_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
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

    LazyColumn(modifier = modifier.fillMaxSize()) {
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
                            onSearch(query)
                            focusManager.clearFocus()
                        },
                        onDelete = onDeleteHistory,
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
                    key = { it.name }
                ) { tag ->
                    val formattedName = TagTranslationProvider.getFormattedName(tag, tagLanguage, "bilingual")
                    val localizedType = when (tag.type.lowercase()) {
                        "artist" -> "作者"
                        "character" -> "角色"
                        "parody" -> "原作"
                        "group" -> "社团"
                        "language" -> "语言"
                        "category" -> "分类"
                        "female" -> "女性"
                        "male" -> "男性"
                        else -> tag.type
                    }
                    ListItem(
                        headlineContent = {
                            Text(text = formattedName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                        },
                        supportingContent = {
                            val subText = if (tag.count > 0) {
                                stringResource(R.string.home_search_tag_sub, localizedType, tag.count)
                            } else {
                                stringResource(R.string.home_search_tag_sub_no_count, localizedType)
                            }
                            Text(text = subText, style = MaterialTheme.typography.bodySmall)
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
                                onSearch(tag.name)
                                focusManager.clearFocus()
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchSortBar(
    sortOption: String,
    onSortOptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    val sortLabelMap = mapOf(
        "date" to stringResource(R.string.sort_date),
        "popular" to stringResource(R.string.sort_popular),
        "popular-today" to stringResource(R.string.sort_popular_today),
        "popular-week" to stringResource(R.string.sort_popular_week),
        "popular-month" to stringResource(R.string.sort_popular_month)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
                            onSortOptionChanged(key)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultGrid(
    searchResults: LazyPagingItems<GalleryListItem>,
    cdnHost: String,
    favoritedIds: Set<Int>,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minSize: Int = 340,
    bottomPadding: Dp = 12.dp
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                columns = StaggeredGridCells.Adaptive(minSize = minSize.dp),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = bottomPadding
                ),
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
                } else if (searchResults.loadState.append is LoadState.Error) {
                    val error = (searchResults.loadState.append as LoadState.Error).error
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error.localizedMessage ?: stringResource(R.string.home_search_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { searchResults.retry() }) {
                                Text(stringResource(R.string.common_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}
