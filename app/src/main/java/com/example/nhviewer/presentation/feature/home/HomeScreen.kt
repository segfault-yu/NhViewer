package com.example.nhviewer.presentation.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.platform.LocalFocusManager
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.SearchHistory
import com.example.nhviewer.presentation.common.EmptyState
import com.example.nhviewer.presentation.feature.profile.ProfileViewModel
import com.example.nhviewer.presentation.feature.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToReader: (Int, Int) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val cdnHost = cdnConfig?.primaryImageHost ?: ""
    val thumbHost = cdnConfig?.primaryThumbHost ?: ""

    val latestGalleries = viewModel.latestGalleries.collectAsLazyPagingItems()
    val popularState by viewModel.popularGalleriesState.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState(initial = emptyList())
    val favoritedIds by viewModel.favoritedIds.collectAsState()

    // 搜索数据与交互状态
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val active by searchViewModel.active.collectAsState()
    val sortOption by searchViewModel.sortOption.collectAsState()
    val searchHistory by searchViewModel.searchHistory.collectAsState(initial = emptyList())
    val autocompleteSuggestions by searchViewModel.autocompleteSuggestions.collectAsState()
    val searchResults = searchViewModel.searchResults.collectAsLazyPagingItems()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("最新", "热门")

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

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is HomeViewModel.HomeNavigationEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.galleryId)
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("清空历史记录") },
            text = { Text("确定要清除所有的搜索历史记录吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        searchViewModel.clearSearchHistory()
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
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }

    Scaffold(
        floatingActionButton = {
            if (!active) {
                FloatingActionButton(
                    onClick = { viewModel.playRandom() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Random"
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部常驻 SearchBar，融合“我的”入口
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (active) Modifier else Modifier.statusBarsPadding())
                    .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchViewModel.onSearchQueryChanged(it) },
                    onSearch = {
                        searchViewModel.search(it)
                        focusManager.clearFocus()
                    },
                    active = active,
                    onActiveChange = { searchViewModel.onActiveChanged(it) },
                    placeholder = { Text("搜索画廊 (例如 pages:>10)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchViewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                            
                            val profileState by profileViewModel.authState.collectAsState()
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp, end = 8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { onOpenDrawer() },
                                contentAlignment = Alignment.Center
                            ) {
                                when (val state = profileState) {
                                    is AuthState.LoggedIn -> {
                                        if (!state.user.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = state.user.avatarUrl,
                                                contentDescription = "Avatar",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                text = state.user.username.take(1).uppercase(),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Profile",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
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
                                            text = "历史记录",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        TextButton(onClick = { showClearHistoryDialog = true }) {
                                            Text("清空全部", style = MaterialTheme.typography.bodySmall)
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
                                            searchViewModel.search(query)
                                            focusManager.clearFocus()
                                        },
                                        onDelete = { query -> searchViewModel.deleteSearchHistory(query) },
                                        onLongPress = { showClearHistoryDialog = true }
                                    )
                                }
                            }
                        } else {
                            if (autocompleteSuggestions.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "搜索建议",
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
                                            Text(text = "类型: ${tag.type}  (${tag.count} 个画廊)", style = MaterialTheme.typography.bodySmall)
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
                                                searchViewModel.search(tag.name)
                                                focusManager.clearFocus()
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 搜索结果页与普通主页内容区域动态切换
            if (!active && searchQuery.isNotEmpty()) {
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
                                        searchViewModel.onSortOptionChanged(key)
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
                            message = error.localizedMessage ?: "搜索发生错误",
                            onRetry = { searchResults.retry() }
                        )
                    } else if (searchResults.itemCount == 0 && searchResults.loadState.refresh is LoadState.NotLoading) {
                        EmptyState(message = "未找到匹配的画廊")
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                top = 12.dp,
                                end = 12.dp,
                                bottom = 12.dp + innerPadding.calculateBottomPadding()
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
                            }
                        }
                    }
                }
            } else if (!active) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> {
                        val pullRefreshState = rememberPullToRefreshState()
                        var isRefreshing by remember { mutableStateOf(false) }
                        LaunchedEffect(latestGalleries.loadState.refresh) {
                            isRefreshing = latestGalleries.loadState.refresh is LoadState.Loading
                        }

                        PullToRefreshBox(
                            state = pullRefreshState,
                            isRefreshing = isRefreshing,
                            onRefresh = { latestGalleries.refresh() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (latestGalleries.loadState.refresh is LoadState.Error) {
                                val error = (latestGalleries.loadState.refresh as LoadState.Error).error
                                ErrorScreen(
                                    message = error.localizedMessage ?: "网络错误",
                                    onRetry = { latestGalleries.retry() }
                                )
                            } else {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        top = 12.dp,
                                        end = 12.dp,
                                        bottom = 12.dp + innerPadding.calculateBottomPadding()
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    items(
                                        count = latestGalleries.itemCount,
                                        key = { index -> latestGalleries[index]?.id ?: index }
                                    ) { index ->
                                        val item = latestGalleries[index]
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

                                    if (latestGalleries.loadState.append is LoadState.Loading) {
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
                                    } else if (latestGalleries.loadState.append is LoadState.Error) {
                                        item(span = StaggeredGridItemSpan.FullLine) {
                                            val error = (latestGalleries.loadState.append as LoadState.Error).error
                                            ErrorScreen(
                                                message = error.localizedMessage ?: "加载下一页失败",
                                                onRetry = { latestGalleries.retry() },
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        val pullRefreshState = rememberPullToRefreshState()
                        val isRefreshing = popularState is HomeViewModel.PopularState.Loading

                        PullToRefreshBox(
                            state = pullRefreshState,
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.loadPopularGalleries() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when (val state = popularState) {
                                is HomeViewModel.PopularState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LoadingIndicator()
                                    }
                                }
                                is HomeViewModel.PopularState.Error -> {
                                    ErrorScreen(
                                        message = state.message,
                                        onRetry = { viewModel.loadPopularGalleries() }
                                    )
                                }
                                is HomeViewModel.PopularState.Success -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 340.dp),
                                        contentPadding = PaddingValues(
                                            start = 12.dp,
                                            top = 12.dp,
                                            end = 12.dp,
                                            bottom = 12.dp + innerPadding.calculateBottomPadding()
                                        ),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            items = state.items,
                                            key = { it.id }
                                        ) { item ->
                                            GalleryCard(
                                                item = item,
                                                cdnHost = cdnHost,
                                                onClick = { onNavigateToDetail(item.id) },
                                                isFavorited = item.id in favoritedIds,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }
                                    }
                                }
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

