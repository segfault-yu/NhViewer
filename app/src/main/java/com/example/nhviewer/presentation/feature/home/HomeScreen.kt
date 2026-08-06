package com.example.nhviewer.presentation.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
import com.example.nhviewer.presentation.common.FastScrollbar
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
import kotlinx.coroutines.launch

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

    // 沉浸式滚动
    var isChromeVisible by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    // 覆盖层实际渲染高度（随字体缩放/内容变化），用于给列表让出等量顶部内边距，避免覆盖层盖住第一项
    var chromeHeightDp by remember { mutableStateOf(0.dp) }
    val chromeHideThresholdPx = with(density) { 8.dp.toPx() }
    val chromeScrollConnection = remember(chromeHideThresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                when {
                    available.y < -chromeHideThresholdPx -> isChromeVisible = false
                    available.y > chromeHideThresholdPx -> isChromeVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // 三个列表各自的下拉状态提到此处：指示器需画在顶部覆盖层之后才不会被盖住
    val searchPullRefreshState = rememberPullToRefreshState()
    val latestPullRefreshState = rememberPullToRefreshState()
    val popularPullRefreshState = rememberPullToRefreshState()

    val tabs = listOf(
        stringResource(R.string.home_tab_latest),
        stringResource(R.string.home_tab_popular)
    )
    val homePagerState = rememberPagerState(pageCount = { tabs.size })
    val homePagerScope = rememberCoroutineScope()
    val selectedTabIndex = homePagerState.currentPage

    // 切换"最新/热门" tab 时重新显示工具区，不带上一个列表滚动到一半的隐藏状态
    LaunchedEffect(selectedTabIndex) {
        isChromeVisible = true
    }

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
            AnimatedVisibility(
                visible = !active && isChromeVisible,
                enter = slideInVertically(animationSpec = NhMotion.Spatial.noBounce()) { it } +
                    fadeIn(animationSpec = NhMotion.Effects.default()),
                exit = slideOutVertically(animationSpec = NhMotion.Spatial.noBounce()) { it } +
                    fadeOut(animationSpec = NhMotion.Effects.default())
            ) {
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 内容层：从屏幕最顶端开始铺满，顶部工具区是浮在它上面的覆盖层
            if (!active && searchQuery.isNotEmpty()) {
                val searchResultGridState = rememberLazyStaggeredGridState()
                // 进入搜索结果态时重置显示状态（同时覆盖了清空关键词后再次搜索的情况）
                LaunchedEffect(Unit) {
                    isChromeVisible = true
                }
                LaunchedEffect(searchResultGridState) {
                    snapshotFlow {
                        searchResultGridState.firstVisibleItemIndex to searchResultGridState.firstVisibleItemScrollOffset
                    }.collect { (index, offset) ->
                        if (index == 0 && offset == 0) isChromeVisible = true
                    }
                }

                SearchResultGrid(
                    searchResults = searchResults,
                    cdnHost = cdnHost,
                    favoritedIds = favoritedIds,
                    onNavigateToDetail = onNavigateToDetail,
                    minSize = 340,
                    topPadding = chromeHeightDp + 12.dp,
                    bottomPadding = 12.dp + innerPadding.calculateBottomPadding(),
                    scrollToTopKey = searchTrigger,
                    gridState = searchResultGridState,
                    scrollConnection = chromeScrollConnection,
                    onRefresh = {
                        searchViewModel.markForceRefresh()
                        searchResults.refresh()
                    },
                    pullRefreshState = searchPullRefreshState
                )
            } else if (!active) {
                HorizontalPager(
                    state = homePagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> {
                            var isRefreshing by remember { mutableStateOf(false) }
                            val latestGridState = rememberLazyStaggeredGridState()
                            LaunchedEffect(latestGalleries.loadState.refresh) {
                                isRefreshing = latestGalleries.loadState.refresh is LoadState.Loading
                            }
                            LaunchedEffect(latestGridState) {
                                snapshotFlow {
                                    latestGridState.firstVisibleItemIndex to latestGridState.firstVisibleItemScrollOffset
                                }.collect { (index, offset) ->
                                    if (index == 0 && offset == 0) isChromeVisible = true
                                }
                            }

                            PullToRefreshBox(
                                state = latestPullRefreshState,
                                isRefreshing = isRefreshing,
                                onRefresh = {
                                    viewModel.markLatestForceRefresh()
                                    latestGalleries.refresh()
                                },
                                modifier = Modifier.fillMaxSize(),
                                // 置空，指示器画在顶部覆盖层之后
                                indicator = {}
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
                                        state = latestGridState,
                                        contentPadding = PaddingValues(
                                            start = 12.dp,
                                            top = chromeHeightDp + 12.dp,
                                            end = 12.dp,
                                            bottom = 12.dp + innerPadding.calculateBottomPadding()
                                        ),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .nestedScroll(chromeScrollConnection)
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

                                    FastScrollbar(
                                        state = latestGridState,
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                            }
                        }
                        1 -> {
                            val isRefreshing = popularState is HomeViewModel.PopularState.Loading
                            val popularGridState = rememberLazyGridState()
                            LaunchedEffect(popularGridState) {
                                snapshotFlow {
                                    popularGridState.firstVisibleItemIndex to popularGridState.firstVisibleItemScrollOffset
                                }.collect { (index, offset) ->
                                    if (index == 0 && offset == 0) isChromeVisible = true
                                }
                            }

                            PullToRefreshBox(
                                state = popularPullRefreshState,
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.loadPopularGalleries(forceRefresh = true) },
                                modifier = Modifier.fillMaxSize(),
                                // 置空，指示器画在顶部覆盖层之后
                                indicator = {}
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
                                            state = popularGridState,
                                            contentPadding = PaddingValues(
                                                start = 12.dp,
                                                top = chromeHeightDp + 12.dp,
                                                end = 12.dp,
                                                bottom = 12.dp + innerPadding.calculateBottomPadding()
                                            ),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .nestedScroll(chromeScrollConnection)
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

                                        FastScrollbar(
                                            state = popularGridState,
                                            modifier = Modifier.align(Alignment.CenterEnd)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 覆盖层：顶部搜索栏 + （tab 栏 或 筛选/排序栏），浮在内容层之上，随 isChromeVisible 隐藏/显示。
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = slideInVertically(animationSpec = NhMotion.Spatial.noBounce()) { -it } +
                    fadeIn(animationSpec = NhMotion.Effects.default()),
                exit = slideOutVertically(animationSpec = NhMotion.Spatial.noBounce()) { -it } +
                    fadeOut(animationSpec = NhMotion.Effects.default()),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .onGloballyPositioned { coordinates ->
                            chromeHeightDp = with(density) { coordinates.size.height.toDp() }
                        }
                ) {
                    // 顶部常驻 SearchBar，融合"我的"入口
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
                    } else if (!active) {
                        PrimaryTabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { homePagerScope.launch { homePagerState.animateScrollToPage(index) } },
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
                    }
                }
            }

            // 指示器画在覆盖层之后，否则会被顶部工具栏完全盖住；工具栏收起时贴顶显示
            if (!active) {
                val (indicatorState, indicatorRefreshing) = when {
                    searchQuery.isNotEmpty() ->
                        searchPullRefreshState to (searchResults.loadState.refresh is LoadState.Loading)
                    selectedTabIndex == 0 ->
                        latestPullRefreshState to (latestGalleries.loadState.refresh is LoadState.Loading)
                    else ->
                        popularPullRefreshState to (popularState is HomeViewModel.PopularState.Loading)
                }
                PullToRefreshDefaults.Indicator(
                    state = indicatorState,
                    isRefreshing = indicatorRefreshing,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = if (isChromeVisible) chromeHeightDp else 0.dp)
                )
            }
        }
    }
}
