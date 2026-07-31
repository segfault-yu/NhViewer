package com.example.nhviewer.presentation.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.nhviewer.R
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.presentation.common.NhSearchBar
import com.example.nhviewer.presentation.common.SearchQueryBuilder
import com.example.nhviewer.presentation.common.SearchResultGrid
import com.example.nhviewer.presentation.common.SearchSortBar
import com.example.nhviewer.presentation.feature.profile.ProfileViewModel
import com.example.nhviewer.presentation.feature.search.SearchViewModel
import com.example.nhviewer.ui.theme.NhMotion
import com.example.nhviewer.util.NetworkErrorParser
import kotlinx.coroutines.flow.collectLatest

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
    val cdnHost = cdnConfig?.primaryThumbHost ?: ""

    val latestGalleries = viewModel.latestGalleries.collectAsLazyPagingItems()
    val popularState by viewModel.popularGalleriesState.collectAsState()
    val favoritedIds by viewModel.favoritedIds.collectAsState()

    // 搜索数据与交互状态
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val active by searchViewModel.active.collectAsState()
    val sortOption by searchViewModel.sortOption.collectAsState()
    val searchHistory by searchViewModel.searchHistory.collectAsState(initial = emptyList())
    val autocompleteSuggestions by searchViewModel.autocompleteSuggestions.collectAsState()
    val searchResults = searchViewModel.searchResults.collectAsLazyPagingItems()
    val totalResults by searchViewModel.totalResults.collectAsState()
    val searchTrigger by searchViewModel.searchTrigger.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.home_tab_latest),
        stringResource(R.string.home_tab_popular)
    )

    val focusManager = LocalFocusManager.current

    // 搜索结果态下返回键先清空搜索、回到 Tab 列表，而非直接退出 App
    BackHandler(enabled = !active && searchQuery.isNotEmpty()) {
        searchViewModel.onSearchQueryChanged("")
    }

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is HomeViewModel.HomeNavigationEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.galleryId)
                }
            }
        }
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
                    .then(
                        if (active) {
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        } else {
                            Modifier.statusBarsPadding()
                        }
                    )
                    .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp)
            ) {
                NhSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchViewModel.onSearchQueryChanged(it) },
                    onSearch = {
                        searchViewModel.search(it)
                        focusManager.clearFocus()
                    },
                    expanded = active,
                    onExpandedChange = { searchViewModel.onActiveChanged(it) },
                    searchHistory = searchHistory,
                    autocompleteSuggestions = autocompleteSuggestions,
                    onDeleteHistory = { searchViewModel.deleteSearchHistory(it) },
                    onClearAllHistory = { searchViewModel.clearSearchHistory() },
                    placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                    leadingIcon = {
                        IconButton(onClick = { onOpenDrawer() }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
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
                            IconButton(onClick = {
                                if (searchQuery.isNotBlank()) {
                                    searchViewModel.search(searchQuery)
                                    focusManager.clearFocus()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            }
                        }
                    }
                )
            }

            // 搜索结果页与普通主页内容区域动态切换
            if (!active && searchQuery.isNotEmpty()) {
                SearchQueryBuilder(
                    rawQuery = searchQuery,
                    onQueryChanged = {
                        searchViewModel.onSearchQueryChanged(it)
                        // 文本改变时不触发搜索，SearchQueryBuilder 内部的选择操作会通过 onTriggerSearch 触发
                    },
                    onTriggerSearch = { query ->
                        searchViewModel.search(query)
                        focusManager.clearFocus()
                    }
                )
                AnimatedVisibility(
                    visible = searchResults.itemCount > 0,
                    enter = fadeIn(animationSpec = NhMotion.Effects.default()),
                    exit = fadeOut(animationSpec = NhMotion.Effects.default())
                ) {
                    SearchSortBar(
                        sortOption = sortOption,
                        onSortOptionChanged = { searchViewModel.onSortOptionChanged(it) },
                        totalCount = totalResults
                    )
                }

                SearchResultGrid(
                    searchResults = searchResults,
                    cdnHost = cdnHost,
                    favoritedIds = favoritedIds,
                    onNavigateToDetail = onNavigateToDetail,
                    minSize = 340,
                    bottomPadding = 12.dp + innerPadding.calculateBottomPadding(),
                    scrollToTopKey = searchTrigger
                )
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
                                        message = NetworkErrorParser.parse(error),
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
                                                    message = NetworkErrorParser.parse(error),
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
                                onRefresh = { viewModel.loadPopularGalleries(forceRefresh = true) },
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
