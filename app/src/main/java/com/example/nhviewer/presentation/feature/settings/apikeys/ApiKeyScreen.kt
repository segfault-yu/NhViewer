package com.example.nhviewer.presentation.feature.settings.apikeys

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nhviewer.presentation.common.CaptchaDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApiKeyViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val apiKeys by viewModel.apiKeys.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val powStatus by viewModel.powStatus.collectAsState()
    val captchaSiteKey by viewModel.captchaSiteKey.collectAsState()
    val newlyCreatedApiKey by viewModel.newlyCreatedApiKey.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var keyNameInput by remember { mutableStateOf("") }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    captchaSiteKey?.let { siteKey ->
        CaptchaDialog(
            siteKey = siteKey,
            onSuccess = { token -> viewModel.onCaptchaSuccess(token) },
            onDismiss = { viewModel.cancelCaptcha() }
        )
    }

    newlyCreatedApiKey?.let { apiKey ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissNewlyCreatedKey() },
            title = { Text("API 密钥申请成功", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "注意！该密钥明文只会展示这一次。如果您离开此页面或关闭提示，明文密钥将永久无法复现。",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = apiKey.key ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("密钥明文 (API Key)") },
                        trailingIcon = {
                            IconButton(onClick = {
                                apiKey.key?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(context, "密钥已复制到剪贴板", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissNewlyCreatedKey() }) {
                    Text("我已保存并关闭")
                }
            }
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                keyNameInput = ""
            },
            title = { Text("申请 API 密钥", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = keyNameInput,
                    onValueChange = { keyNameInput = it },
                    label = { Text("密钥名称/备注") },
                    singleLine = true,
                    placeholder = { Text("例如：我的电脑端") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyNameInput.isNotBlank()) {
                            viewModel.initiateCreateApiKey(keyNameInput)
                            showCreateDialog = false
                            keyNameInput = ""
                        }
                    },
                    enabled = keyNameInput.isNotBlank()
                ) {
                    Text("申请")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    keyNameInput = ""
                }) {
                    Text("取消")
                }
            }
        )
    }

    if (powStatus != "Idle" && powStatus != "等待进行人机验证...") {
        Dialog(onDismissRequest = {}) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = powStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API 密钥管理", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "申请新密钥")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadApiKeys() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (apiKeys.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无 API 密钥，请点击右下角申请",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = apiKeys,
                        key = { it.id }
                    ) { apiKey ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = apiKey.name ?: "未命名密钥",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Column {
                                    Text(
                                        text = "ID: ${apiKey.id}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "创建时间: ${apiKey.createdAt}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { viewModel.revokeApiKey(apiKey.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "吊销密钥",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
