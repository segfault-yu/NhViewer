package com.example.nhviewer.presentation.feature.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nhviewer.presentation.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val readerDirection by settingsViewModel.readerDirection.collectAsState()
    val imageScaleMode by settingsViewModel.imageScaleMode.collectAsState()
    val readerBackground by settingsViewModel.readerBackground.collectAsState()
    val keepScreenOn by settingsViewModel.keepScreenOn.collectAsState()
    val readerBrightness by settingsViewModel.readerBrightness.collectAsState()
    val colorFilterMode by settingsViewModel.colorFilterMode.collectAsState()
    val colorFilterAlpha by settingsViewModel.colorFilterAlpha.collectAsState()
    val pageTransitionAnim by settingsViewModel.pageTransitionAnim.collectAsState()
    val showPersistentPageNumber by settingsViewModel.showPersistentPageNumber.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "阅读器设置",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider()

            // 1. 阅读模式
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "阅读模式",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = readerDirection == "rtl",
                        onClick = { settingsViewModel.setReaderDirection("rtl") },
                        label = { Text("日漫 (RTL)") }
                    )
                    FilterChip(
                        selected = readerDirection == "ltr",
                        onClick = { settingsViewModel.setReaderDirection("ltr") },
                        label = { Text("美漫 (LTR)") }
                    )
                    FilterChip(
                        selected = readerDirection == "vertical",
                        onClick = { settingsViewModel.setReaderDirection("vertical") },
                        label = { Text("条漫 (垂直)") }
                    )
                }
            }

            // 2. 图片缩放模式
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "图片缩放",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = imageScaleMode == "fit_screen",
                        onClick = { settingsViewModel.setImageScaleMode("fit_screen") },
                        label = { Text("适应屏幕") }
                    )
                    FilterChip(
                        selected = imageScaleMode == "fit_width",
                        onClick = { settingsViewModel.setImageScaleMode("fit_width") },
                        label = { Text("适应宽度") }
                    )
                    FilterChip(
                        selected = imageScaleMode == "fit_height",
                        onClick = { settingsViewModel.setImageScaleMode("fit_height") },
                        label = { Text("适应高度") }
                    )
                    FilterChip(
                        selected = imageScaleMode == "original",
                        onClick = { settingsViewModel.setImageScaleMode("original") },
                        label = { Text("原图") }
                    )
                }
            }

            // 3. 背景色
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "背景色",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = readerBackground == "default",
                        onClick = { settingsViewModel.setReaderBackground("default") },
                        label = { Text("默认") }
                    )
                    FilterChip(
                        selected = readerBackground == "amoled",
                        onClick = { settingsViewModel.setReaderBackground("amoled") },
                        label = { Text("纯黑 (AMOLED)") }
                    )
                    FilterChip(
                        selected = readerBackground == "dark_gray",
                        onClick = { settingsViewModel.setReaderBackground("dark_gray") },
                        label = { Text("深灰") }
                    )
                    FilterChip(
                        selected = readerBackground == "white",
                        onClick = { settingsViewModel.setReaderBackground("white") },
                        label = { Text("白色") }
                    )
                }
            }

            // 4. 屏幕亮度
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = SpaceBetween
                ) {
                    Text(
                        text = "屏幕亮度",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "自动",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = readerBrightness == -1f,
                            onCheckedChange = { isAuto ->
                                if (isAuto) {
                                    settingsViewModel.setReaderBrightness(-1f)
                                } else {
                                    settingsViewModel.setReaderBrightness(0.5f)
                                }
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isAuto = readerBrightness == -1f
                    val sliderValue = if (isAuto) 0.5f else readerBrightness
                    Slider(
                        value = sliderValue,
                        onValueChange = { settingsViewModel.setReaderBrightness(it) },
                        valueRange = 0f..1f,
                        enabled = !isAuto,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isAuto) "自动" else "${(sliderValue * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 5. 色彩滤镜
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "色彩滤镜",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = colorFilterMode == "none",
                        onClick = { settingsViewModel.setColorFilterMode("none") },
                        label = { Text("无") }
                    )
                    FilterChip(
                        selected = colorFilterMode == "grayscale",
                        onClick = { settingsViewModel.setColorFilterMode("grayscale") },
                        label = { Text("灰度") }
                    )
                    FilterChip(
                        selected = colorFilterMode == "invert",
                        onClick = { settingsViewModel.setColorFilterMode("invert") },
                        label = { Text("反色") }
                    )
                    FilterChip(
                        selected = colorFilterMode == "sepia",
                        onClick = { settingsViewModel.setColorFilterMode("sepia") },
                        label = { Text("护眼 (琥珀色)") }
                    )
                }
                if (colorFilterMode == "sepia") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "滤镜浓度: ${(colorFilterAlpha * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = colorFilterAlpha,
                        onValueChange = { settingsViewModel.setColorFilterAlpha(it) },
                        valueRange = 0.1f..0.8f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. 辅助开关 (屏幕常亮)
            ListItem(
                headlineContent = {
                    Text(
                        text = "屏幕常亮",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                trailingContent = {
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { settingsViewModel.setKeepScreenOn(it) }
                    )
                },
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "页面过渡动画",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                trailingContent = {
                    Switch(
                        checked = pageTransitionAnim,
                        onCheckedChange = { settingsViewModel.setPageTransitionAnim(it) }
                    )
                },
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            ListItem(
                headlineContent = {
                    Text(
                        text = "常驻显示页码",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                trailingContent = {
                    Switch(
                        checked = showPersistentPageNumber,
                        onCheckedChange = { settingsViewModel.setShowPersistentPageNumber(it) }
                    )
                },
                modifier = Modifier.padding(horizontal = 0.dp)
            )
        }
    }
}
