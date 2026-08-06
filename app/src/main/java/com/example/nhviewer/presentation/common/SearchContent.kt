package com.example.nhviewer.presentation.common

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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
import com.example.nhviewer.ui.theme.NhMotion
import com.example.nhviewer.util.NetworkErrorParser
import com.example.nhviewer.util.RelativeTimeFormatter
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
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CircleShape
                    )
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
        // 水平内边距与区块标题保持 16dp 对齐，垂直 8dp 使相邻条目间距维持原有 16dp
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = history.query,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                Text(
                    text = RelativeTimeFormatter.format(history.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                IconButton(onClick = { onDelete(history.query) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove single",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = CircleShape
                )
                .clip(CircleShape)
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
    onQueryChange: (String) -> Unit,
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
                    val trueType = TagTranslationProvider.getTrueTagType(tag.name, tag.type, tagLanguage).lowercase()
                    val localizedType = when (trueType) {
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

                    val (leadingIcon, iconTint) = when (trueType) {
                        "artist", "character" -> Icons.Default.Person to MaterialTheme.colorScheme.primary
                        "group" -> Icons.Default.Group to MaterialTheme.colorScheme.secondary
                        "category" -> Icons.Default.Category to MaterialTheme.colorScheme.tertiary
                        else -> Icons.AutoMirrored.Filled.Label to MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    ListItem(
                        headlineContent = {
                            Text(
                                text = formattedName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            val subText = if (tag.count > 0) {
                                "$localizedType • ${tag.count} 作品"
                            } else {
                                localizedType
                            }
                            Text(
                                text = subText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = localizedType,
                                tint = iconTint
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {
                                // 只替换"最后一段正在输入的关键词"
                                val prefix = searchQuery.trimEnd().substringBeforeLast(' ', "")
                                val token = TagTranslationProvider.toSearchQueryToken(tag, tagLanguage)
                                val newQuery = if (prefix.isBlank()) "$token " else "$prefix $token "
                                onQueryChange(newQuery)
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
    modifier: Modifier = Modifier,
    totalCount: Int? = null
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
        if (totalCount != null) {
            Text(
                text = "共 $totalCount 个结果",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultGrid(
    searchResults: LazyPagingItems<GalleryListItem>,
    cdnHost: String,
    favoritedIds: Set<Int>,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minSize: Int = 340,
    topPadding: Dp = 12.dp,
    bottomPadding: Dp = 12.dp,
    scrollToTopKey: Any? = null,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    scrollConnection: NestedScrollConnection? = null,
    // 两者同时非 null 才启用下拉刷新。
    // 指示器不在这里绘制：它会被调用方的顶部悬浮工具栏盖住，须由调用方在覆盖层之后自行绘制
    onRefresh: (() -> Unit)? = null,
    pullRefreshState: PullToRefreshState? = null
) {
    // key 用 rememberSaveable 持久化，避免详情页/阅读器返回时因组合被重建而误判为"新搜索"，
    // 把导航返回时应保留的滚动位置又重置回顶部
    var lastHandledScrollToTopKey by rememberSaveable { mutableStateOf<String?>(null) }
    val scrollToTopKeyString = scrollToTopKey?.toString()

    LaunchedEffect(scrollToTopKeyString) {
        if (scrollToTopKeyString != null && scrollToTopKeyString != lastHandledScrollToTopKey) {
            gridState.animateScrollToItem(0)
        }
        lastHandledScrollToTopKey = scrollToTopKeyString
    }

    // 已有结果时刷新失败不会切到错误页，靠 Toast 反馈，否则下拉刷新失败后毫无提示
    val context = LocalContext.current
    val refreshState = searchResults.loadState.refresh
    LaunchedEffect(refreshState) {
        if (refreshState is LoadState.Error && searchResults.itemCount > 0) {
            Toast.makeText(
                context,
                NetworkErrorParser.parse(refreshState.error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val currentState = remember(searchResults.loadState.refresh, searchResults.itemCount) {
        when {
            searchResults.itemCount > 0 -> 3
            searchResults.loadState.refresh is LoadState.Loading -> 0
            searchResults.loadState.refresh is LoadState.Error -> 1
            searchResults.itemCount == 0 && searchResults.loadState.refresh is LoadState.NotLoading -> 2
            else -> 3
        }
    }

    // 状态切换动画整体作为一块内容，按需外包下拉刷新容器
    val stateContent = @Composable {
        AnimatedContent(
            targetState = currentState,
            transitionSpec = {
                fadeIn(animationSpec = NhMotion.Effects.default()) togetherWith
                    fadeOut(animationSpec = NhMotion.Effects.default())
            },
            label = "SearchResultGridState"
        ) { targetState ->
            when (targetState) {
                0 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }
                1 -> {
                    val refreshState = searchResults.loadState.refresh
                    val error = (refreshState as? LoadState.Error)?.error
                    if (error != null) {
                        ErrorScreen(
                            message = NetworkErrorParser.parse(error),
                            onRetry = { searchResults.retry() }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LoadingIndicator()
                        }
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyState(message = stringResource(R.string.home_search_empty))
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "请尝试更少的关键词或移除筛选条件",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = minSize.dp),
                        state = gridState,
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            top = topPadding,
                            end = 12.dp,
                            bottom = bottomPadding
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (scrollConnection != null) {
                                    Modifier.nestedScroll(scrollConnection)
                                } else {
                                    Modifier
                                }
                            )
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
                                        text = NetworkErrorParser.parse(error),
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
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (onRefresh != null && pullRefreshState != null) {
            PullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = refreshState is LoadState.Loading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
                // 置空，指示器由调用方绘制在顶部覆盖层之上
                indicator = {}
            ) {
                stateContent()
            }
        } else {
            stateContent()
        }

        FastScrollbar(
            state = gridState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
