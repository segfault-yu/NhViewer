package com.example.nhviewer.presentation.feature.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nhviewer.presentation.common.EmptyState
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    onNavigateToTaggedGalleries: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TagsViewModel = hiltViewModel()
) {
    val currentType by viewModel.currentType.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val tags = viewModel.tagsFlow.collectAsLazyPagingItems()

    var showSortMenu by remember { mutableStateOf(false) }

    val tabTypes = listOf(
        Pair("tag", "标签"),
        Pair("artist", "画师"),
        Pair("parody", "原作"),
        Pair("character", "角色"),
        Pair("group", "社团"),
        Pair("language", "语言"),
        Pair("category", "分类")
    )

    val currentTabIndex = tabTypes.indexOfFirst { it.first == currentType }.coerceAtLeast(0)

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = currentTabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                tabTypes.forEachIndexed { index, (_, label) ->
                    Tab(
                        selected = currentTabIndex == index,
                        onClick = { viewModel.selectTagType(tabTypes[index].first) },
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (currentTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showSortMenu = true }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (sortOption == "popular") "按热度排序" else "按字母排序",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("按热度排序") },
                            onClick = {
                                viewModel.setSortOption("popular")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("按字母排序") },
                            onClick = {
                                viewModel.setSortOption("name")
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (tags.loadState.refresh is LoadState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else if (tags.loadState.refresh is LoadState.Error) {
                    val error = (tags.loadState.refresh as LoadState.Error).error
                    ErrorScreen(
                        message = error.localizedMessage ?: "加载标签列表失败",
                        onRetry = { tags.retry() }
                    )
                } else if (tags.itemCount == 0 && tags.loadState.refresh is LoadState.NotLoading) {
                    EmptyState(message = "没有找到标签")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 180.dp),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            count = tags.itemCount,
                            key = { index -> tags[index]?.id ?: index }
                        ) { index ->
                            val tag = tags[index]
                            if (tag != null) {
                                OutlinedCard(
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp)
                                        .clickable {
                                            onNavigateToTaggedGalleries(tag.id, tag.name)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = "Tag",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tag.name,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${formatCount(tag.count)} 本书",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (tags.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatCount(count: Int): String {
    return if (count >= 1000) {
        val kValue = count / 1000.0
        String.format("%.1fk", kValue)
    } else {
        count.toString()
    }
}
