package com.example.nhviewer.presentation.feature.reader

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.nhviewer.domain.model.PageInfo

@Composable
fun ThumbnailStrip(
    pages: List<PageInfo>,
    currentPage: Int, // 1-based
    thumbHosts: String,
    onPageClick: (Int) -> Unit, // 1-based
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // 监听 currentPage 变更，平滑滚动让当前项居中
    LaunchedEffect(currentPage) {
        if (pages.isNotEmpty()) {
            val targetIndex = (currentPage - 1).coerceIn(0, pages.size - 1)
            // 滚动到 targetIndex - 3 以尽量保持当前项居中
            val scrollIndex = maxOf(0, targetIndex - 3)
            listState.animateScrollToItem(scrollIndex)
        }
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        itemsIndexed(
            items = pages,
            key = { _, page -> page.number }
        ) { index, page ->
            val isCurrent = (page.number == currentPage)
            val pageUrl = "$thumbHosts/${page.thumbnail}"

            val borderModifier = if (isCurrent) {
                Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
            } else {
                Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .then(borderModifier)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onPageClick(page.number) },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pageUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Page ${page.number}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
