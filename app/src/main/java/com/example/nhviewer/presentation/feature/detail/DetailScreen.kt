package com.example.nhviewer.presentation.feature.detail

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nhviewer.domain.model.AuthState
import com.example.nhviewer.domain.model.GalleryDetail
import com.example.nhviewer.domain.model.Tag
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
                    Toast.makeText(context, "评论发表成功", Toast.LENGTH_SHORT).show()
                }
                is DetailViewModel.DetailUiEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
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

                                // Start / Continue Reading Button + Favorite Button
                                val startPage = readingHistory?.lastReadPage ?: 1
                                val buttonText = if (readingHistory != null) {
                                    "继续阅读 (第 $startPage 页)"
                                } else {
                                    "开始阅读"
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onStartReading(detail.id, startPage) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = "Read"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = buttonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    val isFav by viewModel.isFavorite.collectAsState()
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite() },
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                color = if (isFav) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = "Favorite Toggle",
                                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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

                        // 5. Comments Section Title
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "用户评论",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
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
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            is DetailViewModel.CommentsUiState.Success -> {
                                val list = cState.list
                                if (list.isEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Text(
                                            text = "暂无评论，发条评论抢沙发吧~",
                                            fontSize = 13.sp,
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
                            Spacer(modifier = Modifier.height(16.dp))
                            CommentInputArea(
                                isLoggedIn = currentUserState is AuthState.LoggedIn,
                                onSendComment = { viewModel.startPostComment(it) }
                            )
                            Spacer(modifier = Modifier.height(32.dp))
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
                Dialog(
                    onDismissRequest = {},
                    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            LoadingIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = powStatus,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (!comment.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = comment.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Text(
                            text = comment.username.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = comment.username,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sdf.format(Date(comment.postDate * 1000)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = comment.body,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("复制内容") },
                    onClick = {
                        clipboardManager.setText(AnnotatedString(comment.body))
                        showMenu = false
                    }
                )
                if (currentUserId == comment.userId) {
                    DropdownMenuItem(
                        text = { Text("删除评论", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("举报评论") },
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
                    text = if (isLoggedIn) "发表公开评论..." else "请先登录后发表评论",
                    fontSize = 13.sp
                )
            },
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendComment(text)
                    text = ""
                }
            },
            enabled = isLoggedIn && text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (isLoggedIn && text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "发送",
                tint = if (isLoggedIn && text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
