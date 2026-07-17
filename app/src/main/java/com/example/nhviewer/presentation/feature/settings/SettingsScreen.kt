package com.example.nhviewer.presentation.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class, coil.annotation.ExperimentalCoilApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToSessions: () -> Unit,
    onNavigateToApiKeys: () -> Unit,
    onNavigateToBlacklist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var cacheSize by remember { mutableStateOf("正在计算...") }

    fun formatCacheSize(bytes: Long): String {
        if (bytes <= 0) return "0.0 B"
        val units = listOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    fun updateCacheSize() {
        coroutineScope.launch(Dispatchers.IO) {
            val sizeBytes = context.imageLoader.diskCache?.size ?: 0L
            val formatted = formatCacheSize(sizeBytes)
            withContext(Dispatchers.Main) {
                cacheSize = formatted
            }
        }
    }

    LaunchedEffect(Unit) {
        updateCacheSize()
    }

    val themeMode by viewModel.themeMode.collectAsState()
    val gridBaseWidth by viewModel.gridBaseWidth.collectAsState()
    val readerDirection by viewModel.readerDirection.collectAsState()
    val defaultDownloadFormat by viewModel.defaultDownloadFormat.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()

    var showDownloadFormatDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDirectionDialog by remember { mutableStateOf(false) }

    val themeModeText = when (themeMode) {
        "light" -> "浅色"
        "dark" -> "深色"
        else -> "跟随系统"
    }

    val directionText = when (readerDirection) {
        "ltr" -> "从左向右 (美漫)"
        "vertical" -> "上下滚动 (条漫)"
        else -> "从右向左 (日漫)"
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("主题模式", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = listOf(
                        "system" to "跟随系统",
                        "light" to "浅色",
                        "dark" to "深色"
                    )
                    themes.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(value)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = themeMode == value,
                                onClick = {
                                    viewModel.setThemeMode(value)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Reading Direction Dialog
    if (showDirectionDialog) {
        AlertDialog(
            onDismissRequest = { showDirectionDialog = false },
            title = { Text("默认阅读方向", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val directions = listOf(
                        "rtl" to "从右向左 (日漫)",
                        "ltr" to "从左向右 (美漫)",
                        "vertical" to "上下滚动 (条漫)"
                    )
                    directions.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setReaderDirection(value)
                                    showDirectionDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = readerDirection == value,
                                onClick = {
                                    viewModel.setReaderDirection(value)
                                    showDirectionDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDirectionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Default Download Format Dialog
    if (showDownloadFormatDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadFormatDialog = false },
            title = { Text("默认下载格式", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val formats = listOf("ZIP", "CBZ")
                    formats.forEach { format ->
                        val value = format.lowercase()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultDownloadFormat(value)
                                    showDownloadFormatDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = defaultDownloadFormat == value,
                                onClick = {
                                    viewModel.setDefaultDownloadFormat(value)
                                    showDownloadFormatDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = format, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadFormatDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            SettingsCategoryHeader(title = "账户与安全")
            ListItem(
                headlineContent = {
                    Text(
                        text = "设备与会话管理",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "查看并管理您登录的活跃设备",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToSessions() }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = "API 密钥管理",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "申请和吊销您的 API Token",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToApiKeys() }
            )

            SettingsCategoryHeader(title = "下载与存储")
            ListItem(
                headlineContent = {
                    Text(
                        text = "默认下载格式",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "打包为 ZIP 或 CBZ",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) },
                trailingContent = {
                    Text(
                        text = defaultDownloadFormat.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable { showDownloadFormatDialog = true }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = "缓存清理",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "清理本地 Coil 图片缓存",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) },
                trailingContent = {
                    Text(
                        text = cacheSize,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable {
                    coroutineScope.launch(Dispatchers.IO) {
                        context.imageLoader.diskCache?.clear()
                        val sizeBytes = context.imageLoader.diskCache?.size ?: 0L
                        val formatted = formatCacheSize(sizeBytes)
                        withContext(Dispatchers.Main) {
                            cacheSize = formatted
                            Toast.makeText(context, "缓存清理成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            SettingsCategoryHeader(title = "阅读器偏好")
            ListItem(
                headlineContent = {
                    Text(
                        text = "默认阅读方向",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "当前: $directionText",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.ChromeReaderMode, contentDescription = null) },
                modifier = Modifier.clickable { showDirectionDialog = true }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = "屏幕常亮",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "阅读页面强制保持屏幕常亮",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.ChromeReaderMode, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { viewModel.setKeepScreenOn(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.setKeepScreenOn(!keepScreenOn) }
            )

            SettingsCategoryHeader(title = "外观与显示")
            ListItem(
                headlineContent = {
                    Text(
                        text = "主题模式",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "当前: $themeModeText",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                modifier = Modifier.clickable { showThemeDialog = true }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = "动态取色",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "基于壁纸的主题配色 (Android 12+)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.setDynamicColor(!dynamicColor) }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = "网格密度 (自适应宽度)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Column {
                        Text(
                            text = "当前基准宽度: ${gridBaseWidth}dp",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Slider(
                            value = gridBaseWidth.toFloat(),
                            onValueChange = { viewModel.setGridBaseWidth(it.toInt()) },
                            valueRange = 120f..240f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) }
            )

            SettingsCategoryHeader(title = "内容过滤")
            ListItem(
                headlineContent = {
                    Text(
                        text = "黑名单管理",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = "屏蔽特定标签的画廊",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.FilterList, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToBlacklist() }
            )
        }
    }
}

@Composable
fun SettingsCategoryHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
