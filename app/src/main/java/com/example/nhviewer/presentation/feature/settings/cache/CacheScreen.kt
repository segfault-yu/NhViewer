package com.example.nhviewer.presentation.feature.settings.cache

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhviewer.R
import com.example.nhviewer.presentation.feature.settings.SettingsCategoryHeader
import com.example.nhviewer.util.FileSizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CacheViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val usage by viewModel.usage.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val clearing by viewModel.clearing.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    val calculatingText = stringResource(R.string.settings_calculating)
    fun sizeText(bytes: Long): String = if (loading) calculatingText else FileSizeFormatter.format(bytes)

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.cache_clear_confirm_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.cache_clear_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.clearAll { success ->
                            val messageRes = if (success) {
                                R.string.settings_cache_cleared
                            } else {
                                R.string.cache_clear_partial_failed
                            }
                            Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.common_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_clear_cache_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategoryHeader(title = stringResource(R.string.cache_cat_usage))

            CacheUsageItem(
                title = stringResource(R.string.cache_item_total),
                value = sizeText(usage.totalBytes),
                emphasize = true
            )
            CacheUsageItem(
                title = stringResource(R.string.cache_item_api),
                description = stringResource(R.string.cache_item_api_desc),
                value = sizeText(usage.httpCacheBytes)
            )
            CacheUsageItem(
                title = stringResource(R.string.cache_item_image_disk),
                description = stringResource(R.string.cache_item_image_disk_desc),
                value = sizeText(usage.imageDiskBytes)
            )
            CacheUsageItem(
                title = stringResource(R.string.cache_item_image_memory),
                description = stringResource(R.string.cache_item_image_memory_desc),
                value = sizeText(usage.imageMemoryBytes)
            )
            CacheUsageItem(
                title = stringResource(R.string.cache_item_snapshot),
                description = stringResource(R.string.cache_item_snapshot_desc),
                // 内存快照无法折算字节，展示条目数
                value = if (loading) {
                    calculatingText
                } else {
                    stringResource(R.string.cache_item_snapshot_value, usage.memorySnapshotEntries)
                }
            )

            SettingsCategoryHeader(title = stringResource(R.string.cache_cat_actions))

            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.cache_action_clear_all),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.cache_action_clear_all_desc),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable(enabled = !clearing) { showConfirmDialog = true }
            )

            Text(
                text = stringResource(R.string.cache_clear_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun CacheUsageItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    emphasize: Boolean = false
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                fontWeight = if (emphasize) FontWeight.Bold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = description?.let {
            {
                Text(
                    text = it,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (emphasize) FontWeight.Bold else null,
                color = if (emphasize) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        modifier = modifier
    )
}
