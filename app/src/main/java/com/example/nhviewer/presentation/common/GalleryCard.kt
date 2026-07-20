package com.example.nhviewer.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nhviewer.domain.model.GalleryListItem
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.LocalTagLanguage

private val COMMON_TAGS_MAP = mapOf(
    29963 to "Chinese",
    12227 to "English",
    6346 to "Japanese",
    17249 to "Translated",
    20905 to "Color",
    33173 to "Sole Female",
    35763 to "Sole Male",
    13720 to "Schoolgirl",
    22942 to "Incest",
    2937 to "Big Breasts",
    90671 to "Original",
    19440 to "Lolicon",
    20525 to "Defloration",
    8010 to "Group",
    10314 to "Schoolboy",
    24201 to "Stockings",
    31386 to "Catgirl",
    25871 to "Lingerie",
    27473 to "Mosaic Censorship",
    8378 to "Glasses",
    8529 to "Glasses Girl",
    14283 to "Anal",
    29859 to "Blowjob",
    7752 to "Schoolgirl Uniform"
)

private val CATEGORY_TAGS_MAP = mapOf(
    33172 to "doujinshi",
    33173 to "manga",
    10606 to "artistcg",
    10607 to "gamecg",
    10608 to "imageset",
    10609 to "cosplay",
    10610 to "asian porn",
    10611 to "non-h",
    10612 to "western",
    10613 to "misc"
)

@Composable
fun GalleryCard(
    item: GalleryListItem,
    cdnHost: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorited: Boolean = false
) {
    val tagLanguage = LocalTagLanguage.current
    val tagDisplayMode = LocalTagDisplayMode.current
    val imageUrl = if (cdnHost.isNotEmpty()) {
        val host = if (cdnHost.startsWith("http")) cdnHost else "https://$cdnHost"
        "$host/${item.thumbnail}"
    } else {
        "https://t.nhentai.net/${item.thumbnail}"
    }

    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .aspectRatio(0.72f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.englishTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isFavorited) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorited",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = item.englishTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!item.japaneseTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.japaneseTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val categoryTagId = item.tagIds.firstOrNull { CATEGORY_TAGS_MAP.containsKey(it) }
                    val categoryRawName = categoryTagId?.let { CATEGORY_TAGS_MAP[it] } ?: "doujinshi"

                    Spacer(modifier = Modifier.height(6.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        maxItemsInEachRow = 3,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 分类常驻
                        TagChip(
                            tag = Tag(id = categoryTagId ?: 0, type = "category", name = categoryRawName),
                            onClick = {},
                            tagLanguage = tagLanguage,
                            tagDisplayMode = tagDisplayMode
                        )

                        // 最多展示 2 个常用普通标签
                        val matchedTags = item.tagIds.mapNotNull { COMMON_TAGS_MAP[it] }.take(2)
                        matchedTags.forEach { tagName ->
                            TagChip(
                                tag = Tag(id = 0, type = "tag", name = tagName),
                                onClick = {},
                                tagLanguage = tagLanguage,
                                tagDisplayMode = tagDisplayMode
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Pages",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " ${item.numPages} P",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                    )

                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " ${item.numFavorites}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
