package com.example.nhviewer.presentation.feature.reader

import android.app.Activity
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import android.view.WindowManager
import com.example.nhviewer.presentation.feature.settings.SettingsViewModel
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.hilt.navigation.compose.hiltViewModel
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.PageInfo
import coil.compose.AsyncImage
import coil.request.CachePolicy
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import me.saket.telephoto.zoomable.zoomable
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.rememberZoomableImageState
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.FALLBACK_IMAGE_HOST
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.presentation.common.normalizeCdnHost
import com.example.nhviewer.ui.theme.NhMotion
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    galleryId: Int,
    startPage: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val readerDirection by settingsViewModel.readerDirection.collectAsState()
    val readerBackground by settingsViewModel.readerBackground.collectAsState()
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsState()
    val secureMode by settingsViewModel.secureMode.collectAsState()
    val readerBrightness by settingsViewModel.readerBrightness.collectAsState()
    val colorFilterMode by settingsViewModel.colorFilterMode.collectAsState()
    val colorFilterAlpha by settingsViewModel.colorFilterAlpha.collectAsState()
    val pageTransitionAnim by settingsViewModel.pageTransitionAnim.collectAsState()
    var isOrientationLocked by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val isScrollMode = (readerDirection == "vertical")

    DisposableEffect(keepScreenOn) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null && keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    DisposableEffect(secureMode) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            if (secureMode) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    DisposableEffect(readerBrightness) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val lp = window.attributes
            if (readerBrightness >= 0f) {
                lp.screenBrightness = readerBrightness
            } else {
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            window.attributes = lp
        }
        onDispose {
            if (window != null) {
                val lp = window.attributes
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = lp
            }
        }
    }

    DisposableEffect(isOrientationLocked) {
        val activity = context as? Activity
        if (activity != null && isOrientationLocked) {
            val currentOrientation = activity.resources.configuration.orientation
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val cdnHost = cdnConfig?.primaryImageHost ?: ""

    // 退出阅读器时恢复系统状态栏与导航栏
    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity
            val window = activity?.window
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

    val backgroundColor = when (readerBackground) {
        "amoled" -> androidx.compose.ui.graphics.Color.Black
        "dark_gray" -> androidx.compose.ui.graphics.Color(0xFF1C1C1E)
        "white" -> androidx.compose.ui.graphics.Color.White
        else -> MaterialTheme.colorScheme.background
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
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

                // 预加载逻辑：防抖、无 OOM 风险、且在取消时自动 disposal 以释放带宽
                LaunchedEffect(currentPage, cdnHost) {
                    val loader = context.imageLoader
                    val pages = detail.pages
                    val hosts = normalizeCdnHost(cdnHost, FALLBACK_IMAGE_HOST)

                    val disposables = mutableListOf<coil.request.Disposable>()

                    try {
                        delay(300)

                        // 1. 预加载前后各 1 页
                        val immediateRange = listOf(currentPage - 1, currentPage + 1)
                        for (p in immediateRange) {
                            if (p in 1..detail.numPages) {
                                val page = pages.getOrNull(p - 1)
                                if (page != null) {
                                    val url = "$hosts/${page.path}"
                                    val request = ImageRequest.Builder(context)
                                        .data(url)
                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .build()
                                    val disposable = loader.enqueue(request)
                                    disposables.add(disposable)
                                }
                            }
                        }

                        delay(300)

                        // 2. 预加载前后各 2 页
                        val outerRange = listOf(currentPage - 2, currentPage + 2)
                        for (p in outerRange) {
                            if (p in 1..detail.numPages) {
                                val page = pages.getOrNull(p - 1)
                                if (page != null) {
                                    val url = "$hosts/${page.path}"
                                    val request = ImageRequest.Builder(context)
                                        .data(url)
                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .build()
                                    val disposable = loader.enqueue(request)
                                    disposables.add(disposable)
                                }
                            }
                        }

                        kotlinx.coroutines.awaitCancellation()
                    } finally {
                        disposables.forEach { it.dispose() }
                    }
                }

                ReaderContent(
                    detail = detail,
                    galleryId = galleryId,
                    startPage = startPage,
                    cdnHost = cdnHost,
                    currentPage = currentPage,
                    isScrollMode = isScrollMode,
                    readerDirection = readerDirection,
                    readerBackground = readerBackground,
                    colorFilterMode = colorFilterMode,
                    colorFilterAlpha = colorFilterAlpha,
                    pageTransitionAnim = pageTransitionAnim,
                    isOrientationLocked = isOrientationLocked,
                    onToggleOrientationLock = { isOrientationLocked = !isOrientationLocked },
                    onPageChanged = { page -> viewModel.onPageChanged(galleryId, page) },
                    onBackClick = onBackClick,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderContent(
    detail: GalleryDetail,
    galleryId: Int,
    startPage: Int,
    cdnHost: String,
    currentPage: Int,
    isScrollMode: Boolean,
    readerDirection: String,
    readerBackground: String,
    colorFilterMode: String,
    colorFilterAlpha: Float,
    pageTransitionAnim: Boolean,
    isOrientationLocked: Boolean,
    onToggleOrientationLock: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onBackClick: () -> Unit,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var showOverlays by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var overlayProgress by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current

    // 第 2 级返回：工具栏显示时，手势进度驱动顶/底工具栏跟手滑出；设置面板打开时让位给它自己的预见式返回
    PredictiveBackHandler(enabled = showOverlays && !showSettingsSheet) { progress ->
        try {
            progress.collect { backEvent -> overlayProgress = backEvent.progress }
            showOverlays = false
            overlayProgress = 0f
        } catch (e: CancellationException) {
            overlayProgress = 0f
            throw e
        }
    }

    DisposableEffect(showOverlays) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (showOverlays) {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {}
    }
    val hosts = normalizeCdnHost(cdnHost, FALLBACK_IMAGE_HOST)

    val scope = rememberCoroutineScope()

    val imageScaleMode by settingsViewModel.imageScaleMode.collectAsState()
    val showPersistentPageNumber by settingsViewModel.showPersistentPageNumber.collectAsState()
    val contentScale = when (imageScaleMode) {
        "fit_width" -> ContentScale.FillWidth
        "fit_height" -> ContentScale.FillHeight
        "original" -> ContentScale.None
        else -> ContentScale.Fit
    }

    val sheetState = rememberModalBottomSheetState()

    val filterModifier = Modifier.drawWithContent {
        if (colorFilterMode == "none") {
            drawContent()
        } else {
            val paint = Paint().apply {
                colorFilter = when (colorFilterMode) {
                    "grayscale" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    "invert" -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )))
                    else -> null
                }
            }
            drawIntoCanvas { canvas ->
                canvas.withSaveLayer(Rect(0f, 0f, size.width, size.height), paint) {
                    drawContent()
                    if (colorFilterMode == "sepia") {
                        drawRect(
                            color = Color(0xFFF4ECD8).copy(alpha = colorFilterAlpha),
                            size = size
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isScrollMode) {
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentPage - 1)
            
            val firstVisibleIndexWithOffset by remember {
                derivedStateOf {
                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                    if (visibleItems.isEmpty()) return@derivedStateOf currentPage - 1

                    val firstItem = visibleItems.first()
                    val firstVisibleIndex = firstItem.index
                    val offset = listState.firstVisibleItemScrollOffset
                    val size = firstItem.size

                    if (size > 0 && offset > size / 2) {
                        firstVisibleIndex + 1
                    } else {
                        firstVisibleIndex
                    }
                }
            }

            LaunchedEffect(firstVisibleIndexWithOffset) {
                onPageChanged(firstVisibleIndexWithOffset + 1)
            }

            LaunchedEffect(listState.isScrollInProgress) {
                if (listState.isScrollInProgress) {
                    showOverlays = false
                }
            }

            val zoomableState = rememberZoomableState()
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val height = constraints.maxHeight.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(
                            state = zoomableState,
                            onClick = { offset ->
                                if ((zoomableState.zoomFraction ?: 1f) <= 1.05f) {
                                    val topZone = height * 0.15f
                                    val bottomZone = height * 0.85f
                                    if (offset.y < topZone) {
                                        scope.launch {
                                            listState.animateScrollBy(-height * 0.8f)
                                        }
                                    } else if (offset.y > bottomZone) {
                                        scope.launch {
                                            listState.animateScrollBy(height * 0.8f)
                                        }
                                    } else {
                                        showOverlays = !showOverlays
                                    }
                                }
                            }
                        )
                        .then(filterModifier)
                ) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = detail.pages,
                        key = { _, page -> page.number }
                    ) { _, page ->
                        val thumbHosts = hosts.replace("://i", "://t")
                        ReaderPageItem(
                            page = page,
                            totalPages = detail.pages.size,
                            hosts = hosts,
                            thumbHosts = thumbHosts,
                            contentScale = contentScale,
                            isScrollMode = true,
                            zoomableState = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            }

            LaunchedEffect(currentPage) {
                if (firstVisibleIndexWithOffset != currentPage - 1) {
                    listState.scrollToItem(currentPage - 1)
                }
            }

        } else {
            val pagerState = rememberPagerState(
                initialPage = currentPage - 1,
                pageCount = { detail.pages.size }
            )
            
            LaunchedEffect(pagerState.currentPage) {
                onPageChanged(pagerState.currentPage + 1)
            }

            LaunchedEffect(pagerState.isScrollInProgress) {
                if (pagerState.isScrollInProgress) {
                    showOverlays = false
                }
            }

            HorizontalPager(
                state = pagerState,
                reverseLayout = (readerDirection == "rtl"),
                modifier = Modifier.fillMaxSize().then(filterModifier)
            ) { pageIndex ->
                val page = detail.pages[pageIndex]
                val pageUrl = "$hosts/${page.path}"

                val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                val scale = if (pageTransitionAnim) (1f - (pageOffset * 0.15f).coerceIn(0f, 0.15f)) else 1f
                val alpha = if (pageTransitionAnim) (1f - (pageOffset * 0.5f).coerceIn(0f, 0.5f)) else 1f

                val zoomableState = rememberZoomableState()

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val width = constraints.maxWidth.toFloat()
                    
                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage != pageIndex) {
                            zoomableState.resetZoom(animationSpec = androidx.compose.animation.core.snap())
                        }
                    }
                    val thumbHosts = hosts.replace("://i", "://t")
                    ReaderPageItem(
                        page = page,
                        totalPages = detail.pages.size,
                        hosts = hosts,
                        thumbHosts = thumbHosts,
                        contentScale = contentScale,
                        isScrollMode = false,
                        zoomableState = zoomableState,
                        modifier = Modifier.fillMaxSize(),
                        onTap = { offset ->
                            if ((zoomableState.zoomFraction ?: 1f) <= 1.05f) {
                                val leftZone = width * 0.25f
                                val rightZone = width * 0.75f
                                if (offset.x < leftZone) {
                                    if (readerDirection == "rtl") {
                                        scope.launch {
                                            if (pagerState.currentPage < pagerState.pageCount - 1) {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            if (pagerState.currentPage > 0) {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        }
                                    }
                                } else if (offset.x > rightZone) {
                                    if (readerDirection == "rtl") {
                                        scope.launch {
                                            if (pagerState.currentPage > 0) {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            if (pagerState.currentPage < pagerState.pageCount - 1) {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    }
                                } else {
                                    showOverlays = !showOverlays
                                }
                            }
                        }
                    )
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
            enter = slideInVertically(animationSpec = NhMotion.Spatial.default()) { -it } +
                fadeIn(animationSpec = NhMotion.Effects.default()),
            exit = slideOutVertically(animationSpec = NhMotion.Spatial.default()) { -it } +
                fadeOut(animationSpec = NhMotion.Effects.default()),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    translationY = -overlayProgress * size.height
                    alpha = 1f - overlayProgress
                }
        ) {
            ReaderTopBar(
                title = detail.englishTitle,
                onBackClick = onBackClick
            )
        }

        AnimatedVisibility(
            visible = showOverlays,
            enter = slideInVertically(animationSpec = NhMotion.Spatial.default()) { it } +
                fadeIn(animationSpec = NhMotion.Effects.default()),
            exit = slideOutVertically(animationSpec = NhMotion.Spatial.default()) { it } +
                fadeOut(animationSpec = NhMotion.Effects.default()),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = overlayProgress * size.height
                    alpha = 1f - overlayProgress
                }
        ) {
            val thumbHosts = hosts.replace("://i", "://t")
            ReaderBottomBar(
                currentPage = currentPage,
                totalPages = detail.numPages,
                pages = detail.pages,
                thumbHosts = thumbHosts,
                isOrientationLocked = isOrientationLocked,
                onToggleOrientationLock = onToggleOrientationLock,
                onSettingsClick = { showSettingsSheet = true },
                onPageSeek = { targetPage -> onPageChanged(targetPage) }
            )
        }

        // 常驻页码提示胶囊
        AnimatedVisibility(
            visible = showPersistentPageNumber && !showOverlays,
            enter = fadeIn(animationSpec = NhMotion.Effects.default()),
            exit = fadeOut(animationSpec = NhMotion.Effects.default()),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(bottom = 16.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape
            ) {
                Text(
                    text = "$currentPage / ${detail.pages.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (showSettingsSheet) {
        ReaderSettingsSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            settingsViewModel = settingsViewModel
        )
    }
}

@Composable
fun ReaderTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            shape = CircleShape,
            shadowElevation = 8.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onBackClick,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun ReaderBottomBar(
    currentPage: Int,
    totalPages: Int,
    pages: List<PageInfo>,
    thumbHosts: String,
    isOrientationLocked: Boolean,
    onToggleOrientationLock: () -> Unit,
    onSettingsClick: () -> Unit,
    onPageSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                ThumbnailStrip(
                    pages = pages,
                    currentPage = currentPage,
                    thumbHosts = thumbHosts,
                    onPageClick = onPageSeek,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "$currentPage / $totalPages",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    FilledTonalIconButton(
                        onClick = onToggleOrientationLock,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isOrientationLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (isOrientationLocked) "Unlock Orientation" else "Lock Orientation"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = onSettingsClick,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            }
        }
    }
}
