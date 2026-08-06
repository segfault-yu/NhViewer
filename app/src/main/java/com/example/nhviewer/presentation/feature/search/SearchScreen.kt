package com.example.nhviewer.presentation.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
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

    // 沉浸式滚动
    var isChromeVisible by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    // 覆盖层实际渲染高度（随字体缩放/内容变化），用于给结果网格让出等量顶部内边距，避免覆盖层盖住第一项
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
    val searchResultGridState = rememberLazyStaggeredGridState()
    val searchPullRefreshState = rememberPullToRefreshState()
    // 新一次搜索（关键词/筛选/排序变化）时重置显示状态
    LaunchedEffect(searchTrigger) {
        isChromeVisible = true
    }
    LaunchedEffect(searchResultGridState) {
        snapshotFlow {
            searchResultGridState.firstVisibleItemIndex to searchResultGridState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            if (index == 0 && offset == 0) isChromeVisible = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 内容层：从屏幕最顶端开始铺满，顶部工具区是浮在它上面的覆盖层
        // 隐藏/显示工具区不会导致内容跟着重新布局挪位（做法与阅读器顶/底工具栏一致
        if (!active) {
            SearchResultGrid(
                searchResults = searchResults,
                cdnHost = cdnHost,
                favoritedIds = favoritedIds,
                onNavigateToDetail = onNavigateToDetail,
                topPadding = chromeHeightDp + 12.dp,
                scrollToTopKey = searchTrigger,
                gridState = searchResultGridState,
                scrollConnection = chromeScrollConnection,
                onRefresh = {
                    viewModel.markForceRefresh()
                    searchResults.refresh()
                },
                pullRefreshState = searchPullRefreshState
            )
        }

        // 覆盖层：顶部搜索栏 + 筛选/排序栏，浮在内容层之上，随 isChromeVisible 隐藏/显示
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

                if (!active && searchQuery.isNotEmpty()) {
                    SearchQueryBuilder(
                        rawQuery = searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onTriggerSearch = { query ->
                            viewModel.search(query)
                            focusManager.clearFocus()
                        }
                    )
                }
                if (!active) {
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
                }
            }
        }

        // 指示器画在覆盖层之后，否则会被顶部工具栏完全盖住；工具栏收起时贴顶显示
        if (!active) {
            PullToRefreshDefaults.Indicator(
                state = searchPullRefreshState,
                isRefreshing = searchResults.loadState.refresh is LoadState.Loading,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (isChromeVisible) chromeHeightDp else 0.dp)
            )
        }
    }
}
