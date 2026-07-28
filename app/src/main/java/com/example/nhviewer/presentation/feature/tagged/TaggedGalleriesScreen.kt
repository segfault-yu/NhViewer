package com.example.nhviewer.presentation.feature.tagged

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nhviewer.presentation.common.ErrorScreen
import com.example.nhviewer.presentation.common.GalleryCard
import com.example.nhviewer.presentation.common.LoadingIndicator
import com.example.nhviewer.util.NetworkErrorParser

import androidx.compose.ui.res.stringResource
import com.example.nhviewer.R
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.LocalTagLanguage
import com.example.nhviewer.util.i18n.TagTranslationProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaggedGalleriesScreen(
    tagId: Int,
    tagName: String,
    onBackClick: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaggedGalleriesViewModel = hiltViewModel()
) {
    val galleries = viewModel.galleries.collectAsLazyPagingItems()
    val cdnConfig by viewModel.cdnConfig.collectAsState()
    val cdnHost = cdnConfig?.primaryThumbHost ?: ""
    val tagLanguage = LocalTagLanguage.current
    val tagDisplayMode = LocalTagDisplayMode.current

    LaunchedEffect(tagId) {
        viewModel.setTagId(tagId)
    }

    val displayTagName = TagTranslationProvider.getFormattedName(tagName, tagLanguage, tagDisplayMode)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tagged_title, displayTagName), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (galleries.loadState.refresh is LoadState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }
            } else if (galleries.loadState.refresh is LoadState.Error) {
                val error = (galleries.loadState.refresh as LoadState.Error).error
                ErrorScreen(
                    message = NetworkErrorParser.parse(error),
                    onRetry = { galleries.retry() }
                )
            } else if (galleries.itemCount == 0 && galleries.loadState.refresh is LoadState.NotLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.tagged_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = galleries.itemCount,
                        key = { index -> galleries[index]?.id ?: index }
                    ) { index ->
                        val item = galleries[index]
                        if (item != null) {
                            GalleryCard(
                                item = item,
                                cdnHost = cdnHost,
                                onClick = { onNavigateToDetail(item.id) },
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    if (galleries.loadState.append is LoadState.Loading) {
                        item(span = StaggeredGridItemSpan.FullLine) {
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
