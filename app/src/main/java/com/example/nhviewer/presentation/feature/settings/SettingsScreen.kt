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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import com.example.nhviewer.util.log.AppLogger
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
import androidx.compose.ui.res.stringResource
import com.example.nhviewer.R
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class, coil.annotation.ExperimentalCoilApi::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToSessions: () -> Unit,
    onNavigateToApiKeys: () -> Unit,
    onNavigateToBlacklist: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var cacheSize by remember { mutableStateOf(context.getString(R.string.settings_calculating)) }
    var logsSize by remember { mutableStateOf(context.getString(R.string.settings_calculating)) }

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
            val logBytes = AppLogger.getTotalLogSize(context)
            val logFormatted = formatCacheSize(logBytes)
            withContext(Dispatchers.Main) {
                cacheSize = formatted
                logsSize = logFormatted
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
    val secureMode by viewModel.secureMode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val tagLanguage by viewModel.tagLanguage.collectAsState()
    val tagDisplayMode by viewModel.tagDisplayMode.collectAsState()
    val logLevel by viewModel.logLevel.collectAsState()

    var showDownloadFormatDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showDirectionDialog by remember { mutableStateOf(false) }
    var showAppLanguageDialog by remember { mutableStateOf(false) }
    var showTagLanguageDialog by remember { mutableStateOf(false) }
    var showLogLevelDialog by remember { mutableStateOf(false) }

    val themeModeText = when (themeMode) {
        "light" -> stringResource(R.string.settings_theme_light)
        "dark" -> stringResource(R.string.settings_theme_dark)
        else -> stringResource(R.string.settings_theme_system)
    }

    val directionText = when (readerDirection) {
        "ltr" -> stringResource(R.string.reader_mode_ltr)
        "vertical" -> stringResource(R.string.reader_mode_vertical)
        else -> stringResource(R.string.reader_mode_rtl)
    }

    val appLanguageText = when (appLanguage) {
        "zh" -> stringResource(R.string.settings_app_language_zh)
        "en" -> stringResource(R.string.settings_app_language_en)
        else -> stringResource(R.string.settings_app_language_system)
    }

    val tagDisplayModeText = when (tagDisplayMode) {
        "only_original" -> stringResource(R.string.settings_tag_mode_only_original)
        "bilingual" -> stringResource(R.string.settings_tag_mode_bilingual)
        else -> stringResource(R.string.settings_tag_mode_only_translation)
    }

    val logLevelText = when (logLevel) {
        "debug" -> stringResource(R.string.settings_log_level_debug)
        "warn" -> stringResource(R.string.settings_log_level_warn)
        "error" -> stringResource(R.string.settings_log_level_error)
        "none" -> stringResource(R.string.settings_log_level_none)
        else -> stringResource(R.string.settings_log_level_info)
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = listOf(
                        "system" to stringResource(R.string.settings_theme_system),
                        "light" to stringResource(R.string.settings_theme_light),
                        "dark" to stringResource(R.string.settings_theme_dark)
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
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Reading Direction Dialog
    if (showDirectionDialog) {
        AlertDialog(
            onDismissRequest = { showDirectionDialog = false },
            title = { Text(stringResource(R.string.settings_reader_direction_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val directions = listOf(
                        "rtl" to stringResource(R.string.reader_mode_rtl),
                        "ltr" to stringResource(R.string.reader_mode_ltr),
                        "vertical" to stringResource(R.string.reader_mode_vertical)
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
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Default Download Format Dialog
    if (showDownloadFormatDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadFormatDialog = false },
            title = { Text(stringResource(R.string.settings_download_format_title), fontWeight = FontWeight.Bold) },
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
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // App Language Dialog
    if (showAppLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showAppLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_app_language_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(
                        "system" to stringResource(R.string.settings_app_language_system),
                        "zh" to stringResource(R.string.settings_app_language_zh),
                        "en" to stringResource(R.string.settings_app_language_en)
                    )
                    options.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppLanguage(value)
                                    showAppLanguageDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = appLanguage == value,
                                onClick = {
                                    viewModel.setAppLanguage(value)
                                    showAppLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppLanguageDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Tag Display Mode Dialog
    if (showTagLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showTagLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_tag_language_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(
                        "only_translation" to stringResource(R.string.settings_tag_mode_only_translation),
                        "only_original" to stringResource(R.string.settings_tag_mode_only_original),
                        "bilingual" to stringResource(R.string.settings_tag_mode_bilingual)
                    )
                    options.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTagDisplayMode(value)
                                    showTagLanguageDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = tagDisplayMode == value,
                                onClick = {
                                    viewModel.setTagDisplayMode(value)
                                    showTagLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagLanguageDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Log Level Dialog
    if (showLogLevelDialog) {
        AlertDialog(
            onDismissRequest = { showLogLevelDialog = false },
            title = { Text(stringResource(R.string.settings_log_level_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(
                        "info" to stringResource(R.string.settings_log_level_info),
                        "debug" to stringResource(R.string.settings_log_level_debug),
                        "warn" to stringResource(R.string.settings_log_level_warn),
                        "error" to stringResource(R.string.settings_log_level_error),
                        "none" to stringResource(R.string.settings_log_level_none)
                    )
                    options.forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLogLevel(value)
                                    showLogLevelDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = logLevel == value,
                                onClick = {
                                    viewModel.setLogLevel(value)
                                    showLogLevelDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogLevelDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_account))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_sessions_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_sessions_desc),
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
                        text = stringResource(R.string.settings_apikeys_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_apikeys_desc),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToApiKeys() }
            )

            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_download))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_download_format_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_download_format_desc),
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
                        text = stringResource(R.string.settings_clear_cache_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_clear_cache_desc),
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
                            Toast.makeText(context, context.getString(R.string.settings_cache_cleared), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_logs))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_log_level_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = logLevelText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.BugReport, contentDescription = null) },
                modifier = Modifier.clickable { showLogLevelDialog = true }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_export_logs_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_export_logs_desc),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                trailingContent = {
                    Text(
                        text = logsSize,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable {
                    // 扫描目录与构造 FileProvider uri 都是磁盘操作，不能放在主线程
                    coroutineScope.launch(Dispatchers.IO) {
                        val logFiles = AppLogger.getLogFiles(context)
                        val uris = try {
                            ArrayList(
                                logFiles.map { file ->
                                    androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                }
                            )
                        } catch (e: Exception) {
                            AppLogger.e("Settings", "构建日志分享链接失败", e)
                            null
                        }
                        withContext(Dispatchers.Main) {
                            when {
                                uris == null -> Toast.makeText(
                                    context,
                                    context.getString(R.string.settings_export_logs_failed),
                                    Toast.LENGTH_SHORT
                                ).show()

                                uris.isEmpty() -> Toast.makeText(
                                    context,
                                    context.getString(R.string.settings_no_logs),
                                    Toast.LENGTH_SHORT
                                ).show()

                                else -> {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                                        type = "text/plain"
                                        putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = android.content.Intent.createChooser(
                                        intent,
                                        context.getString(R.string.settings_export_logs_title)
                                    )
                                    // 无可处理分享的应用时 startActivity 会抛异常
                                    try {
                                        context.startActivity(chooser)
                                    } catch (e: Exception) {
                                        AppLogger.e("Settings", "拉起日志分享失败", e)
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.settings_export_logs_failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_clear_logs_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_clear_logs_desc),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                modifier = Modifier.clickable {
                    AppLogger.clearAllLogs(context)
                    logsSize = "0.0 B"
                    Toast.makeText(context, context.getString(R.string.settings_logs_cleared), Toast.LENGTH_SHORT).show()
                }
            )

            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_reader))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_reader_direction_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_current_fmt, directionText),
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
                        text = stringResource(R.string.reader_keep_screen_on),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_keep_screen_on_desc),
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
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_secure_mode_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_secure_mode_desc),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.ChromeReaderMode, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = secureMode,
                        onCheckedChange = { viewModel.setSecureMode(it) }
                    )
                },
                modifier = Modifier.clickable { viewModel.setSecureMode(!secureMode) }
            )

            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_appearance))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_theme_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_current_fmt, themeModeText),
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
                        text = stringResource(R.string.settings_dynamic_color_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_dynamic_color_desc),
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
                        text = stringResource(R.string.settings_grid_density_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Column {
                        Text(
                            text = stringResource(R.string.settings_grid_base_width, gridBaseWidth),
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

            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_filter))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.blacklist_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_blacklist_desc),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.FilterList, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToBlacklist() }
            )

            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_language))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_app_language_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_current_fmt, appLanguageText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                modifier = Modifier.clickable { showAppLanguageDialog = true }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_tag_language_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_current_fmt, tagDisplayModeText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Translate, contentDescription = null) },
                modifier = Modifier.clickable { showTagLanguageDialog = true }
            )

            SettingsCategoryHeader(title = stringResource(R.string.settings_cat_about))
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.settings_about_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(R.string.settings_about_desc),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                modifier = Modifier.clickable { onNavigateToAbout() }
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
