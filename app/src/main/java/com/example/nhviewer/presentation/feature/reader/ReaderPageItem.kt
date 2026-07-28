package com.example.nhviewer.presentation.feature.reader

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.nhviewer.domain.model.PageInfo
import androidx.compose.ui.res.stringResource
import com.example.nhviewer.R
import com.example.nhviewer.ui.theme.NhMotion
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState

@Composable
fun ReaderPageItem(
    page: PageInfo,
    totalPages: Int,
    hosts: String,
    thumbHosts: String,
    contentScale: ContentScale,
    isScrollMode: Boolean,
    zoomableState: ZoomableState?,
    modifier: Modifier = Modifier,
    onTap: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null
) {
    var retryTrigger by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val pageUrl = "$hosts/${page.path}"
    val thumbnailUrl = "$thumbHosts/${page.thumbnail}"

    val imageRequest = remember(pageUrl, retryTrigger) {
        ImageRequest.Builder(context)
            .data(pageUrl)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    val painter = rememberAsyncImagePainter(model = imageRequest)
    val painterState = painter.state

    // 加载状态历史锁
    // 若在初次组合时 painterState 已是 Success (例如存在于内存缓存中)，直接标为 loaded，防止闪烁
    var hasLoaded by remember(pageUrl) { mutableStateOf(painterState is AsyncImagePainter.State.Success) }

    LaunchedEffect(painterState) {
        if (painterState is AsyncImagePainter.State.Success) {
            hasLoaded = true
        }
    }

    // 渐变过渡动画
    val placeholderAlpha by animateFloatAsState(
        targetValue = if (hasLoaded) 0f else 1f,
        animationSpec = NhMotion.Effects.default(),
        label = "placeholderFade"
    )

    val containerModifier = if (isScrollMode) {
        Modifier
            .fillMaxWidth()
            .aspectRatio(page.width.toFloat() / page.height.toFloat())
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = modifier.then(containerModifier),
        contentAlignment = Alignment.Center
    ) {
        // 1. 高清主图（底层，先于遮罩渲染）
        if (isScrollMode) {
            Image(
                painter = painter,
                contentDescription = "Page ${page.number}",
                contentScale = contentScale,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            if (zoomableState != null) {
                val imageState = rememberZoomableImageState(zoomableState)
                val isTelephotoSuccess = imageState.isImageDisplayed
                LaunchedEffect(isTelephotoSuccess) {
                    if (isTelephotoSuccess) {
                        hasLoaded = true
                    }
                }

                ZoomableAsyncImage(
                    model = imageRequest,
                    contentDescription = "Page ${page.number}",
                    state = imageState,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                    onClick = onTap
                )
            }
        }

        // 2. 加载占位层（上层，带 300ms 平滑淡出效果）
        if (placeholderAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = placeholderAlpha }
            ) {
                // 空白底色背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                )

                // 原生加载动画 / 错误重试（居中）
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (painterState is AsyncImagePainter.State.Error) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { retryTrigger++ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.common_retry),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.common_error_load_failed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}
