package com.example.nhviewer.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nhviewer.domain.model.GalleryListItem

@Composable
fun GalleryCard(
    item: GalleryListItem,
    cdnHost: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = if (cdnHost.isNotEmpty()) {
        val host = if (cdnHost.startsWith("http")) cdnHost else "https://$cdnHost"
        "$host/${item.thumbnail}"
    } else {
        // Fallback：使用默认缩略图 CDN
        "https://t.nhentai.net/${item.thumbnail}"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            val ratio = if (item.thumbnailWidth > 0 && item.thumbnailHeight > 0) {
                item.thumbnailWidth.toFloat() / item.thumbnailHeight.toFloat()
            } else {
                0.7f
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = item.englishTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.englishTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                        text = "${item.numPages} P",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(14.dp).height(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.numFavorites.toString(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
