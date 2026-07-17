package com.example.nhviewer.presentation.feature.reader

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import com.example.nhviewer.domain.model.GalleryDetail
import coil.compose.AsyncImage
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    galleryId: Int,
    startPage: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val isScrollMode by viewModel.isScrollMode.collectAsState()

    val cdnHost = cdnConfig?.primaryImageHost ?: ""

    // 沉浸式全屏支持（隐去状态栏与导航栏）
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            viewModel.flushFinalHistory(galleryId)
        }
    }

    LaunchedEffect(galleryId) {
        viewModel.loadGallery(galleryId, startPage)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = detailState) {
            is ReaderViewModel.ReaderUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            }
            is ReaderViewModel.ReaderUiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.loadGallery(galleryId, startPage) }
                )
            }
            is ReaderViewModel.ReaderUiState.Success -> {
                val detail = state.detail

                // 图片预加载
                LaunchedEffect(currentPage, cdnHost) {
                    delay(300)

                    val loader = context.imageLoader
                    val pages = detail.pages
                    val hosts = if (cdnHost.isNotEmpty()) {
                        if (cdnHost.startsWith("http")) cdnHost else "https://$cdnHost"
                    } else {
                        "https://i.nhentai.net"
                    }

                    val immediateRange = listOf(currentPage - 1, currentPage + 1)
                    for (p in immediateRange) {
                        if (p in 1..detail.numPages) {
                            val path = pages.find { it.number == p }?.path
                            if (path != null) {
                                val url = "$hosts/$path"
                                val request = ImageRequest.Builder(context)
                                    .data(url)
                                    .build()
                                loader.enqueue(request)
                            }
                        }
                    }

                    delay(300)
                    val outerRange = listOf(currentPage - 2, currentPage + 2)
                    for (p in outerRange) {
                        if (p in 1..detail.numPages) {
                            val path = pages.find { it.number == p }?.path
                            if (path != null) {
                                val url = "$hosts/$path"
                                val request = ImageRequest.Builder(context)
                                    .data(url)
                                    .build()
                                loader.enqueue(request)
                            }
                        }
                    }
                }

                ReaderContent(
                    detail = detail,
                    galleryId = galleryId,
                    startPage = startPage,
                    cdnHost = cdnHost,
                    currentPage = currentPage,
                    isScrollMode = isScrollMode,
                    onPageChanged = { page -> viewModel.onPageChanged(galleryId, page) },
                    onBackClick = onBackClick,
                    toggleReadingMode = { viewModel.toggleReadingMode() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderContent(
    detail: GalleryDetail,
    galleryId: Int,
    startPage: Int,
    cdnHost: String,
    currentPage: Int,
    isScrollMode: Boolean,
    onPageChanged: (Int) -> Unit,
    onBackClick: () -> Unit,
    toggleReadingMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOverlays by remember { mutableStateOf(true) }
    val hosts = if (cdnHost.isNotEmpty()) {
        if (cdnHost.startsWith("http")) cdnHost else "https://$cdnHost"
    } else {
        "https://i.nhentai.net"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showOverlays = !showOverlays
            }
    ) {
        if (isScrollMode) {
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = startPage - 1)
            val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

            LaunchedEffect(firstVisibleIndex) {
                onPageChanged(firstVisibleIndex + 1)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = detail.pages,
                    key = { _, page -> page.number }
                ) { _, page ->
                    val pageUrl = "$hosts/${page.path}"
                    ZoomableBox(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(pageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Page ${page.number}",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            LaunchedEffect(currentPage) {
                if (firstVisibleIndex != currentPage - 1) {
                    listState.scrollToItem(currentPage - 1)
                }
            }

        } else {
            val pagerState = rememberPagerState(
                initialPage = startPage - 1,
                pageCount = { detail.pages.size }
            )

            LaunchedEffect(pagerState.currentPage) {
                onPageChanged(pagerState.currentPage + 1)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val page = detail.pages[pageIndex]
                val pageUrl = "$hosts/${page.path}"

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ZoomableBox(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(pageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Page ${page.number}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            LaunchedEffect(currentPage) {
                if (pagerState.currentPage != currentPage - 1) {
                    pagerState.scrollToPage(currentPage - 1)
                }
            }
        }

        // Overlay Toolbars
        AnimatedVisibility(
            visible = showOverlays,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReaderTopBar(
                title = detail.prettyTitle ?: detail.englishTitle,
                onBackClick = onBackClick
            )
        }

        AnimatedVisibility(
            visible = showOverlays,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderBottomBar(
                currentPage = currentPage,
                totalPages = detail.numPages,
                isScrollMode = isScrollMode,
                toggleReadingMode = toggleReadingMode,
                onPageSeek = { targetPage -> onPageChanged(targetPage) }
            )
        }
    }
}

@Composable
fun ReaderTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReaderBottomBar(
    currentPage: Int,
    totalPages: Int,
    isScrollMode: Boolean,
    toggleReadingMode: () -> Unit,
    onPageSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$currentPage / $totalPages",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = toggleReadingMode) {
                Icon(
                    imageVector = if (isScrollMode) Icons.Default.SwapVert else Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Toggle Reading Mode",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentPage.toFloat(),
            onValueChange = { onPageSeek(it.toInt()) },
            valueRange = 1f..totalPages.toFloat(),
            steps = if (totalPages > 2) totalPages - 2 else 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
