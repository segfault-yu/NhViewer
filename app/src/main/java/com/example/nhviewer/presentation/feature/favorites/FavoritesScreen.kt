package com.example.nhviewer.presentation.feature.favorites

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nhviewer.presentation.common.EmptyState
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.domain.model.AuthState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val cdnHost = cdnConfig?.primaryThumbHost ?: ""
    val favorites = viewModel.favoritesPagingData.collectAsLazyPagingItems()

    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(favorites.loadState.refresh) {
        isRefreshing = favorites.loadState.refresh is LoadState.Loading
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is FavoritesViewModel.FavoritesUiEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.galleryId)
                }
                is FavoritesViewModel.FavoritesUiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "我的收藏", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.playRandomFavorite() },
                        enabled = favorites.itemCount > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "随机收藏"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (authState is AuthState.LoggedOut) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = "Not Logged In",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "请先登录以查看我的收藏",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToAuth
                    ) {
                        Text(text = "去登录")
                    }
                }
            }
        } else {
            PullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = { favorites.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (favorites.loadState.refresh is LoadState.Error) {
                    val error = (favorites.loadState.refresh as LoadState.Error).error
                    ErrorScreen(
                        message = error.localizedMessage ?: "拉取收藏夹失败",
                        onRetry = { favorites.retry() }
                    )
                } else if (favorites.itemCount == 0 && favorites.loadState.refresh is LoadState.NotLoading) {
                    EmptyState(
                        message = "收藏夹是空的",
                        subtitle = "在画廊详情页点击收藏，即可在此处查看",
                        icon = Icons.Rounded.FavoriteBorder
                    )
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = favorites.itemCount,
                            key = { index -> favorites[index]?.id ?: index }
                        ) { index ->
                            val item = favorites[index]
                            if (item != null) {
                                GalleryCard(
                                    item = item,
                                    cdnHost = cdnHost,
                                    onClick = { onNavigateToDetail(item.id) },
                                    isFavorited = true,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }

                        if (favorites.loadState.append is LoadState.Loading) {
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
                        } else if (favorites.loadState.append is LoadState.Error) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                val error = (favorites.loadState.append as LoadState.Error).error
                                ErrorScreen(
                                    message = error.localizedMessage ?: "加载更多失败",
                                    onRetry = { favorites.retry() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
