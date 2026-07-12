package com.example.nhviewer.presentation.feature.detail

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.presentation.common.TagChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    galleryId: Int,
    onBackClick: () -> Unit,
    onStartReading: (Int, Int) -> Unit,
    onTagClick: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val relatedState by viewModel.relatedState.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()

    val cdnHost = cdnConfig?.primaryImageHost ?: ""

    LaunchedEffect(galleryId) {
        viewModel.loadGalleryDetail(galleryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "画廊详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = detailState) {
                is DetailViewModel.DetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }
                is DetailViewModel.DetailUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.loadGalleryDetail(galleryId) }
                    )
                }
                is DetailViewModel.DetailUiState.Success -> {
                    val detail = state.detail
                    val coverUrl = if (cdnHost.isNotEmpty()) {
                        val host = if (cdnHost.startsWith("http")) cdnHost else "https://$cdnHost"
                        "$host/${detail.coverPath}"
                    } else {
                        "https://t.nhentai.net/${detail.coverPath}"
                    }

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Gallery Info Header
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val coverRatio = if (detail.coverWidth > 0 && detail.coverHeight > 0) {
                                        detail.coverWidth.toFloat() / detail.coverHeight.toFloat()
                                    } else {
                                        0.7f
                                    }

                                    AsyncImage(
                                        model = coverUrl,
                                        contentDescription = detail.englishTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(0.4f)
                                            .aspectRatio(coverRatio)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(
                                        modifier = Modifier.weight(0.6f)
                                    ) {
                                        Text(
                                            text = detail.prettyTitle ?: detail.englishTitle,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )

                                        if (!detail.japaneseTitle.isNullOrEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = detail.japaneseTitle,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = "Pages",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.width(14.dp).height(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${detail.numPages} 页",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Favorites",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(14.dp).height(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${detail.numFavorites} 收藏",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        val uploadDateStr = sdf.format(Date(detail.uploadDate * 1000))

                                        Text(
                                            text = "上传时间: $uploadDateStr",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Start / Continue Reading Button
                                val startPage = readingHistory?.lastReadPage ?: 1
                                val buttonText = if (readingHistory != null) {
                                    "继续阅读 (第 $startPage 页)"
                                } else {
                                    "开始阅读"
                                }

                                Button(
                                    onClick = { onStartReading(detail.id, startPage) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = "Read"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }

                        // 2. Tag Sections grouped by type
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Column {
                                Text(
                                    text = "标签列表",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TagGroupSection(
                                    tags = detail.tags,
                                    onTagClick = { tag ->
                                        onTagClick(tag.id, tag.name)
                                    }
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        // 3. Related Galleries Header
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = "相关推荐",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // 4. Related Galleries Grid Items (Waterfall)
                        when (val relState = relatedState) {
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
                                        text = "相关推荐加载失败",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            is DetailViewModel.RelatedUiState.Success -> {
                                items(
                                    items = relState.list,
                                    key = { it.id }
                                ) { relatedItem ->
                                    GalleryCard(
                                        item = relatedItem,
                                        cdnHost = cdnHost,
                                        onClick = {
                                            viewModel.loadGalleryDetail(relatedItem.id)
                                        },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagGroupSection(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedTags = tags.groupBy { it.type }
    val displayNames = mapOf(
        "artist" to "画师 (Artist)",
        "character" to "角色 (Character)",
        "parody" to "原作 (Parody)",
        "group" to "社团 (Group)",
        "language" to "语言 (Language)",
        "category" to "分类 (Category)",
        "tag" to "标签 (Tag)"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        groupedTags.forEach { (type, tagList) ->
            val displayName = displayNames[type.lowercase()] ?: type.uppercase()
            Column {
                Text(
                    text = displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagList.forEach { tag ->
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
