package com.example.nhviewer.presentation.feature.detail

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.example.nhviewer.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.LocalTagLanguage
import com.example.nhviewer.util.i18n.TagTranslationProvider
import com.example.nhviewer.presentation.common.CaptchaDialog
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.presentation.common.TagChip
import kotlinx.coroutines.flow.collectLatest
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
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detailState by viewModel.detailState.collectAsState()
    val relatedState by viewModel.relatedState.collectAsState()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()
    val currentUserState by viewModel.authState.collectAsState()

    val cdnHost = cdnConfig?.primaryImageHost ?: ""

    LaunchedEffect(galleryId) {
        viewModel.loadGalleryDetail(galleryId)
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DetailViewModel.DetailUiEvent.CommentPostedSuccess -> {
                    Toast.makeText(context, context.getString(R.string.detail_comment_success), Toast.LENGTH_SHORT).show()
                }
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
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
                    val coverUrl = if (cdnHost.isNotEmpty()) {
                        val host = if (cdnHost.startsWith("http")) cdnHost else "https://$cdnHost"
                        "$host/${detail.coverPath}"
                    } else {
                        "https://t.nhentai.net/${detail.coverPath}"
                    }

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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .widthIn(max = 600.dp)
                                ) {
                                    val coverRatio = if (detail.coverWidth > 0 && detail.coverHeight > 0) {
                                        detail.coverWidth.toFloat() / detail.coverHeight.toFloat()
                                    } else {
                                        0.7f
                                    }

                                    ElevatedCard(
                                        shape = MaterialTheme.shapes.medium,
                                        modifier = Modifier
                                            .weight(0.4f)
                                            .aspectRatio(coverRatio)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(coverUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = detail.englishTitle,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(MaterialTheme.shapes.medium)
                                        )
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .weight(0.6f)
                                            .padding(start = 16.dp)
                                    ) {
                                        Text(
                                            text = detail.prettyTitle ?: detail.englishTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )

                                        if (!detail.japaneseTitle.isNullOrEmpty()) {
                                            Text(
                                                text = detail.japaneseTitle,
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
                                                text = stringResource(R.string.detail_pages_count, detail.numPages),
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
                                                text = stringResource(R.string.detail_favorites_count, detail.numFavorites),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }

                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        val uploadDateStr = sdf.format(Date(detail.uploadDate * 1000))

                                        Text(
                                            text = stringResource(R.string.detail_upload_time, uploadDateStr),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        val categoryTag = detail.tags.firstOrNull { it.type.equals("category", ignoreCase = true) }
                                        val categoryFormatted = if (categoryTag != null) {
                                            TagTranslationProvider.getFormattedName(categoryTag, tagLanguage, tagDisplayMode)
                                        } else {
                                            "Doujinshi"
                                        }

                                        androidx.compose.material3.Surface(
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = categoryFormatted,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // Start / Continue Reading Button + Favorite Button
                                val startPage = readingHistory?.lastReadPage ?: 1
                                val buttonText = if (readingHistory != null) {
                                    stringResource(R.string.detail_continue_reading, startPage)
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
                                        onClick = { onStartReading(detail.id, startPage) },
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

                                    val isFav by viewModel.isFavorite.collectAsState()
                                    FilledTonalIconToggleButton(
                                        checked = isFav,
                                        onCheckedChange = { viewModel.toggleFavorite() },
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                            .size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = "Favorite Toggle"
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Tag Sections grouped by type
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
                                    TagGroupSection(
                                        tags = detail.tags,
                                        onTagClick = { tag ->
                                            onTagClick(tag.id, tag.name)
                                        }
                                    )
                                }
                            }
                        }

                        // 3. Related Galleries Header
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = stringResource(R.string.detail_section_recommendations),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 12.dp)
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
                                        text = stringResource(R.string.detail_recommendations_failed),
                                        style = MaterialTheme.typography.bodyMedium,
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
                                            onNavigateToDetail(relatedItem.id)
                                        },
                                        modifier = Modifier.padding(6.dp),
                                        showTags = false,
                                        isGridMode = true
                                    )
                                }
                            }
                        }

                        // 5. Comments Section Title
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = stringResource(R.string.detail_section_comments),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        // 6. Comments List
                        when (val cState = commentsState) {
                            is DetailViewModel.CommentsUiState.Loading -> {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LoadingIndicator()
                                    }
                                }
                            }
                            is DetailViewModel.CommentsUiState.Error -> {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Text(
                                        text = cState.message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            is DetailViewModel.CommentsUiState.Success -> {
                                val list = cState.list
                                if (list.isEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Text(
                                            text = stringResource(R.string.detail_comments_empty),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp)
                                        )
                                    }
                                } else {
                                    items(
                                        items = list,
                                        key = { it.id },
                                        span = { StaggeredGridItemSpan.FullLine }
                                    ) { comment ->
                                        CommentItemRow(
                                            comment = comment,
                                            currentUserId = (currentUserState as? AuthState.LoggedIn)?.user?.id,
                                            onDelete = { viewModel.deleteComment(comment.id) },
                                            onReport = { viewModel.reportComment(comment.id) },
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 7. Post Comment Input Area
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 16.dp, bottom = 32.dp)
                                    .imePadding()
                            ) {
                                CommentInputArea(
                                    isLoggedIn = currentUserState is AuthState.LoggedIn,
                                    onSendComment = { viewModel.startPostComment(it) }
                                )
                            }
                        }
                    }
                }
            }

            // PoW + CAPTCHA Turnstile Dialog barrier
            val powStatus by viewModel.powStatus.collectAsState()
            val captchaSiteKey by viewModel.captchaSiteKey.collectAsState()

            captchaSiteKey?.let { siteKey ->
                CaptchaDialog(
                    siteKey = siteKey,
                    onSuccess = { token -> viewModel.onCaptchaSuccess(token) },
                    onDismiss = { viewModel.cancelCaptcha() }
                )
            }

            if (powStatus != "Idle" && captchaSiteKey == null) {
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {},
                    title = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            LoadingIndicator(modifier = Modifier.size(48.dp))
                        }
                    },
                    text = {
                        Text(
                            text = powStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    shape = MaterialTheme.shapes.extraLarge
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
    val tagLanguage = com.example.nhviewer.util.i18n.LocalTagLanguage.current

    // 目标显示顺序: 语言 -> 原作 -> 角色 -> 画师 -> 社团 -> 女性 -> 男性 -> 其他
    val targetOrder = listOf("language", "parody", "character", "artist", "group", "female", "male", "tag")

    // 过滤掉 category 标签，并解析真正的标签类型 (female/male)
    val validTags = tags.filterNot { it.type.equals("category", ignoreCase = true) }
    val groupedTags = validTags.groupBy { tag -> 
        val trueType = com.example.nhviewer.util.i18n.TagTranslationProvider.getTrueTagType(tag.name, tag.type, tagLanguage).lowercase()
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
                    // 左侧固定类型胶囊徽章 (如 原作、角色、女性、其他)，固定于首行顶部，与右侧胶囊对齐
                    // 使用 48.dp 的 Box 补足 SuggestionChip 默认的最小触摸目标高度偏差
                    Box(
                        modifier = Modifier.height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = labelName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentItemRow(
    comment: com.example.nhviewer.domain.model.Comment,
    currentUserId: Int?,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                leadingContent = {
                    if (!comment.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = comment.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Text(
                                text = comment.username.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                headlineContent = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comment.username,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sdf.format(Date(comment.postDate * 1000)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                supportingContent = {
                    Text(
                        text = comment.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.detail_comment_copy)) },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(comment.body))
                        showMenu = false
                    }
                )
                if (currentUserId == comment.userId) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.detail_comment_delete), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.detail_comment_report)) },
                        onClick = {
                            onReport()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentInputArea(
    isLoggedIn: Boolean,
    onSendComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            enabled = isLoggedIn,
            placeholder = {
                Text(
                    text = if (isLoggedIn) stringResource(R.string.detail_comment_placeholder_logged_in) else stringResource(R.string.detail_comment_placeholder_guest),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            shape = MaterialTheme.shapes.extraLarge,
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.weight(1f)
        )

        FilledIconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendComment(text)
                    text = ""
                }
            },
            enabled = isLoggedIn && text.isNotBlank(),
            modifier = Modifier
                .padding(start = 8.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(R.string.detail_comment_send)
            )
        }
    }
}
