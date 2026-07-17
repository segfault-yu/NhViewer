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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToReader: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val cdnHost = cdnConfig?.primaryImageHost ?: ""
    val thumbHost = cdnConfig?.primaryThumbHost ?: ""

    val latestGalleries = viewModel.latestGalleries.collectAsLazyPagingItems()
    val popularState by viewModel.popularGalleriesState.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState(initial = emptyList())
    val favoritedIds by viewModel.favoritedIds.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("最新", "热门")

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
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                                    columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
                                    contentPadding = PaddingValues(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (readingHistory.isNotEmpty()) {
                                        item(span = StaggeredGridItemSpan.FullLine) {
                                            HistorySection(
                                                historyList = readingHistory,
                                                cdnHost = cdnHost,
                                                thumbHost = thumbHost,
                                                onHistoryClick = onNavigateToReader
                                            )
                                        }
                                    }

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
                                        columns = GridCells.Adaptive(minSize = 160.dp),
                                        contentPadding = PaddingValues(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (readingHistory.isNotEmpty()) {
                                            item(span = { GridItemSpan(maxLineSpan) }) {
                                                HistorySection(
                                                    historyList = readingHistory,
                                                    cdnHost = cdnHost,
                                                    thumbHost = thumbHost,
                                                    onHistoryClick = onNavigateToReader
                                                )
                                            }
                                        }

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

@Composable
fun HistorySection(
    historyList: List<ReadingHistory>,
    cdnHost: String,
    thumbHost: String,
    onHistoryClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "最近阅读",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = historyList,
                key = { it.galleryId }
            ) { history ->
                HistoryCard(
                    history = history,
                    thumbHost = thumbHost,
                    onClick = { onHistoryClick(history.galleryId, history.lastReadPage) }
                )
            }
        }
    }
}

@Composable
fun HistoryCard(
    history: ReadingHistory,
    thumbHost: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val host = when {
        thumbHost.startsWith("http") -> thumbHost
        thumbHost.isNotEmpty() -> "https://$thumbHost"
        else -> "https://t.nhentai.net"
    }
    val imageUrl = "$host/galleries/${history.mediaId}/thumb.webp"

    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .width(260.dp)
            .clickable { onClick() }
    ) {
        ListItem(
            leadingContent = {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = history.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 50.dp, height = 70.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                )
            },
            headlineContent = {
                Text(
                    text = history.title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = "第 ${history.lastReadPage} / ${history.totalPages} 页",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}
