package com.example.nhviewer.presentation.feature.detail

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nhviewer.R
import com.example.nhviewer.domain.model.PageInfo
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.presentation.common.DEFAULT_COVER_RATIO
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.FALLBACK_THUMB_HOST
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.presentation.common.TagChip
import com.example.nhviewer.presentation.common.aspectRatioOf
import com.example.nhviewer.presentation.common.buildGalleryImageUrl
import com.example.nhviewer.presentation.common.normalizeCdnHost
import com.example.nhviewer.presentation.navigation.galleryCardSharedBounds
import com.example.nhviewer.presentation.navigation.galleryCoverSharedElement
import com.example.nhviewer.presentation.navigation.gallerySkipToLookaheadSize
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.LocalTagLanguage
import com.example.nhviewer.util.i18n.TagTranslationProvider
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    galleryId: Int,
    onBackClick: () -> Unit,
    onStartReading: (Int, Int) -> Unit,
    onTagClick: (Tag) -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val relatedState by viewModel.relatedState.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val previewItem by viewModel.previewItem.collectAsState()

    // 封面、页面预览、推荐卡片都是缩略图资源，统一走 thumb host；image host 只供阅读器加载原图
    val thumbHost = normalizeCdnHost(cdnConfig?.primaryThumbHost, FALLBACK_THUMB_HOST)

    LaunchedEffect(galleryId) {
        viewModel.loadGalleryDetail(galleryId)
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DetailViewModel.DetailUiEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is DetailViewModel.DetailUiEvent.ShowMessageRes -> {
                    Toast.makeText(context, context.getString(event.resId), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.detail_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = detailState) {
                is DetailViewModel.DetailUiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // 与 Success 分支同结构的封面占位符：挂上相同的 sharedElement/sharedBounds key，
                        // 让 Hero 转场在详情数据还没返回时也有目标可以变形，不必等网络请求完成
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        ) {
                            ElevatedCard(
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .weight(0.4f)
                                    .aspectRatio(DEFAULT_COVER_RATIO)
                                    .galleryCardSharedBounds(galleryId)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .galleryCoverSharedElement(galleryId)
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.6f))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            LoadingIndicator()
                        }
                    }
                }
                is DetailViewModel.DetailUiState.Preview -> {
                    val item = state.item

                    // 骨架结构与 Success 分支共用同一套 LazyVerticalStaggeredGrid + contentPadding，
                    // 避免详情数据到达后从 Column 切到网格布局导致内容跳动
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = innerPadding.calculateTopPadding() + 16.dp,
                            end = 16.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            GalleryDetailHeader(
                                galleryId = item.id,
                                // 与列表卡片同一张图，Coil 内存缓存直接命中，预填首屏无需等待网络
                                coverUrl = buildGalleryImageUrl(thumbHost, item.thumbnail),
                                coverPlaceholderUrl = null,
                                coverRatio = aspectRatioOf(item.thumbnailWidth, item.thumbnailHeight),
                                title = item.englishTitle,
                                japaneseTitle = item.japaneseTitle,
                                numPages = item.numPages,
                                numFavorites = item.numFavorites,
                                uploadDate = item.uploadDate,
                                category = null,
                                readingHistory = readingHistory,
                                isFavorite = isFavorite,
                                // 详情未到达前收藏无法乐观更新收藏数，先禁用
                                favoriteEnabled = false,
                                onStartReading = { onStartReading(item.id, readingHistory?.lastReadPage ?: 1) },
                                onToggleFavorite = {},
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // 卡片数据里没有完整标签，骨架阶段固定展示局部 loading，等详情接口一次性补齐
                        tagsSection(
                            tags = emptyList(),
                            isLoading = true,
                            onTagClick = onTagClick
                        )

                        // 相关推荐走独立接口，骨架阶段就已提前发起
                        relatedSection(
                            relatedState = relatedState,
                            thumbHost = thumbHost,
                            onNavigateToDetail = onNavigateToDetail
                        )
                    }
                }
                is DetailViewModel.DetailUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ErrorScreen(
                            message = state.message,
                            onRetry = { viewModel.loadGalleryDetail(galleryId) }
                        )
                    }
                }
                is DetailViewModel.DetailUiState.Success -> {
                    val tagLanguage = LocalTagLanguage.current
                    val tagDisplayMode = LocalTagDisplayMode.current
                    val detail = state.detail
                    val coverUrl = buildGalleryImageUrl(thumbHost, detail.coverPath)

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = innerPadding.calculateTopPadding() + 16.dp,
                            end = 16.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Gallery Info Header
                        item(span = StaggeredGridItemSpan.FullLine) {
                            val categoryTag = detail.tags.firstOrNull { it.type.equals("category", ignoreCase = true) }
                            val categoryFormatted = if (categoryTag != null) {
                                TagTranslationProvider.getFormattedName(categoryTag, tagLanguage, tagDisplayMode)
                            } else {
                                "Doujinshi"
                            }

                            GalleryDetailHeader(
                                galleryId = detail.id,
                                coverUrl = coverUrl,
                                // 大图落地前沿用列表缩略图，避免从预填切到详情时闪一下灰块
                                coverPlaceholderUrl = previewItem?.let { buildGalleryImageUrl(thumbHost, it.thumbnail) },
                                coverRatio = aspectRatioOf(detail.coverWidth, detail.coverHeight),
                                title = detail.englishTitle,
                                japaneseTitle = detail.japaneseTitle,
                                numPages = detail.numPages,
                                numFavorites = detail.numFavorites,
                                uploadDate = detail.uploadDate,
                                category = categoryFormatted,
                                readingHistory = readingHistory,
                                isFavorite = isFavorite,
                                favoriteEnabled = true,
                                onStartReading = { onStartReading(detail.id, readingHistory?.lastReadPage ?: 1) },
                                onToggleFavorite = { viewModel.toggleFavorite() },
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // 2. Tag Sections grouped by type
                        tagsSection(
                            tags = detail.tags,
                            isLoading = false,
                            onTagClick = onTagClick
                        )

                        // 3. Page Preview Section (缩略图网格预览，只有详情接口才有 pages 数据)
                        if (detail.pages.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                PagePreviewSection(
                                    pages = detail.pages,
                                    thumbHost = thumbHost,
                                    onPageClick = { pageNum ->
                                        onStartReading(detail.id, pageNum)
                                    }
                                )
                            }
                        }

                        // 4 & 5. Related Galleries Header + Grid Items (Waterfall)
                        relatedSection(
                            relatedState = relatedState,
                            thumbHost = thumbHost,
                            onNavigateToDetail = onNavigateToDetail
                        )
                    }
                }
            }
        }
    }
}

/**
 * 标签区块。Preview 与 Success 分支共用，骨架阶段标签由 tagIds 单独查得，
 * isLoading 为 true 时展示局部 loading 而不是让整个区块空着。
 */
private fun LazyStaggeredGridScope.tagsSection(
    tags: List<Tag>,
    isLoading: Boolean,
    onTagClick: (Tag) -> Unit
) {
    item(span = StaggeredGridItemSpan.FullLine) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.detail_section_tags),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(modifier = Modifier.padding(top = 8.dp)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                } else {
                    TagGroupSection(
                        tags = tags,
                        onTagClick = onTagClick
                    )
                }
            }
        }
    }
}

/**
 * 相关推荐区块（含标题）。Preview 与 Success 分支共用，走独立接口，
 * 骨架阶段就已提前发起请求，不依赖详情接口是否返回。
 */
private fun LazyStaggeredGridScope.relatedSection(
    relatedState: DetailViewModel.RelatedUiState,
    thumbHost: String,
    onNavigateToDetail: (Int) -> Unit
) {
    item(span = StaggeredGridItemSpan.FullLine) {
        Text(
            text = stringResource(R.string.detail_section_recommendations),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }

    when (relatedState) {
        is DetailViewModel.RelatedUiState.Loading -> {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        }
        is DetailViewModel.RelatedUiState.Error -> {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    text = stringResource(R.string.detail_recommendations_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is DetailViewModel.RelatedUiState.Success -> {
            items(
                items = relatedState.list,
                key = { it.id }
            ) { relatedItem ->
                GalleryCard(
                    item = relatedItem,
                    cdnHost = thumbHost,
                    onClick = {
                        onNavigateToDetail(relatedItem.id)
                    },
                    modifier = Modifier.padding(6.dp),
                    showTags = false,
                    isGridMode = true
                )
            }
        }
    }
}

/**
 * 详情页头部。列表快照预填与详情数据共用同一结构，
 * 避免两套布局导致数据到达时封面尺寸与文字位置跳动。
 */
@Composable
private fun GalleryDetailHeader(
    galleryId: Int,
    coverUrl: String,
    coverPlaceholderUrl: String?,
    coverRatio: Float,
    title: String,
    japaneseTitle: String?,
    numPages: Int,
    numFavorites: Int,
    uploadDate: Long?,
    category: String?,
    readingHistory: ReadingHistory?,
    isFavorite: Boolean,
    favoriteEnabled: Boolean,
    onStartReading: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
        ) {
            ElevatedCard(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(0.4f)
                    .aspectRatio(coverRatio)
                    .galleryCardSharedBounds(galleryId)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverUrl)
                        .placeholderMemoryCacheKey(coverPlaceholderUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .galleryCoverSharedElement(galleryId)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.gallerySkipToLookaheadSize()
                )

                if (!japaneseTitle.isNullOrEmpty()) {
                    Text(
                        text = japaneseTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Pages",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.detail_pages_count, numPages),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.detail_favorites_count, numFavorites),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // 列表快照可能不带上传时间，缺失时整行不渲染
                if (uploadDate != null) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val uploadDateStr = sdf.format(Date(uploadDate * 1000))

                    Text(
                        text = stringResource(R.string.detail_upload_time, uploadDateStr),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (category != null) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 开始/继续阅读按钮与收藏按钮
        val buttonText = if (readingHistory != null) {
            stringResource(R.string.detail_continue_reading, readingHistory.lastReadPage)
        } else {
            stringResource(R.string.detail_start_reading)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onStartReading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Read"
                )
                Text(
                    text = " $buttonText",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            FilledTonalIconToggleButton(
                checked = isFavorite,
                onCheckedChange = { onToggleFavorite() },
                enabled = favoriteEnabled,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite Toggle"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagGroupSection(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    val tagLanguage = LocalTagLanguage.current

    // 目标显示顺序: 语言 -> 原作 -> 角色 -> 画师 -> 社团 -> 女性 -> 男性 -> 其他
    val targetOrder = listOf("language", "parody", "character", "artist", "group", "female", "male", "tag")

    // 过滤掉 category 标签，并解析真正的标签类型 (female/male)
    val validTags = tags.filterNot { it.type.equals("category", ignoreCase = true) }
    val groupedTags = validTags.groupBy { tag ->
        val trueType = TagTranslationProvider.getTrueTagType(tag.name, tag.type, tagLanguage).lowercase()
        if (targetOrder.contains(trueType)) trueType else "tag"
    }

    val displayNames = mapOf(
        "language" to stringResource(R.string.tag_type_language),
        "parody" to stringResource(R.string.tag_type_parody),
        "character" to stringResource(R.string.tag_type_character),
        "artist" to stringResource(R.string.tag_type_artist),
        "group" to stringResource(R.string.tag_type_group),
        "female" to stringResource(R.string.tag_type_female),
        "male" to stringResource(R.string.tag_type_male),
        "tag" to stringResource(R.string.tag_type_tag)
    )

    val orderedTypes = targetOrder.filter { groupedTags.containsKey(it) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        orderedTypes.forEach { type ->
            val tagList = groupedTags[type] ?: emptyList()
            if (tagList.isNotEmpty()) {
                val sortedTagList = tagList.sortedWith(compareByDescending<Tag> { it.count }.thenBy { it.name })
                val labelName = displayNames[type] ?: type

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 左侧固定类型胶囊徽章 (如 原作、角色、女性、其他)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .width(60.dp)
                            .height(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = labelName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // 右侧标签 Flow 布局
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        sortedTagList.forEach { tag ->
                            TagChip(
                                tag = tag,
                                onClick = { onTagClick(tag) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PagePreviewSection(
    pages: List<PageInfo>,
    thumbHost: String,
    onPageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    BackHandler(enabled = isExpanded) { isExpanded = false }
    val showExpandButton = pages.size > 8
    val visiblePages = if (isExpanded || !showExpandButton) pages else pages.take(8)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_section_pages_preview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            visiblePages.chunked(4).forEach { rowPages ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowPages.forEach { page ->
                        val pageUrl = "$thumbHost/${page.thumbnail}"
                        val ratio = if (page.thumbnailWidth > 0 && page.thumbnailHeight > 0) {
                            page.thumbnailWidth.toFloat() / page.thumbnailHeight.toFloat()
                        } else {
                            0.7f
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPageClick(page.number) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(ratio)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(pageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Page ${page.number}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Text(
                                text = page.number.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    if (rowPages.size < 4) {
                        repeat(4 - rowPages.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (showExpandButton) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                val icon = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isExpanded) {
                        stringResource(R.string.detail_preview_collapse)
                    } else {
                        stringResource(R.string.detail_preview_expand, pages.size)
                    }
                )
            }
        }
    }
}
