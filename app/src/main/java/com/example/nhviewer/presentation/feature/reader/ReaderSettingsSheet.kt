package com.example.nhviewer.presentation.feature.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nhviewer.R
import com.example.nhviewer.presentation.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.reader_settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            // 1. 阅读模式与图片缩放模式
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.reader_mode_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = readerDirection == "rtl",
                                onClick = { settingsViewModel.setReaderDirection("rtl") },
                                label = { Text(stringResource(R.string.reader_mode_rtl)) }
                            )
                            FilterChip(
                                selected = readerDirection == "ltr",
                                onClick = { settingsViewModel.setReaderDirection("ltr") },
                                label = { Text(stringResource(R.string.reader_mode_ltr)) }
                            )
                            FilterChip(
                                selected = readerDirection == "vertical",
                                onClick = { settingsViewModel.setReaderDirection("vertical") },
                                label = { Text(stringResource(R.string.reader_mode_vertical)) }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.reader_scale_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = imageScaleMode == "fit_screen",
                                onClick = { settingsViewModel.setImageScaleMode("fit_screen") },
                                label = { Text(stringResource(R.string.reader_scale_fit_screen)) }
                            )
                            FilterChip(
                                selected = imageScaleMode == "fit_width",
                                onClick = { settingsViewModel.setImageScaleMode("fit_width") },
                                label = { Text(stringResource(R.string.reader_scale_fit_width)) }
                            )
                            FilterChip(
                                selected = imageScaleMode == "fit_height",
                                onClick = { settingsViewModel.setImageScaleMode("fit_height") },
                                label = { Text(stringResource(R.string.reader_scale_fit_height)) }
                            )
                            FilterChip(
                                selected = imageScaleMode == "original",
                                onClick = { settingsViewModel.setImageScaleMode("original") },
                                label = { Text(stringResource(R.string.reader_scale_original)) }
                            )
                        }
                    }
                }
            }

            // 2. 背景色与屏幕亮度
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.reader_bg_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = readerBackground == "default",
                                onClick = { settingsViewModel.setReaderBackground("default") },
                                label = { Text(stringResource(R.string.reader_bg_default)) }
                            )
                            FilterChip(
                                selected = readerBackground == "amoled",
                                onClick = { settingsViewModel.setReaderBackground("amoled") },
                                label = { Text(stringResource(R.string.reader_bg_black)) }
                            )
                            FilterChip(
                                selected = readerBackground == "dark_gray",
                                onClick = { settingsViewModel.setReaderBackground("dark_gray") },
                                label = { Text(stringResource(R.string.reader_bg_dark_gray)) }
                            )
                            FilterChip(
                                selected = readerBackground == "white",
                                onClick = { settingsViewModel.setReaderBackground("white") },
                                label = { Text(stringResource(R.string.reader_bg_white)) }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.reader_brightness_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.reader_brightness_auto),
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
                                text = if (isAuto) stringResource(R.string.reader_brightness_auto) else "${(sliderValue * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 3. 色彩滤镜
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reader_filter_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = colorFilterMode == "none",
                            onClick = { settingsViewModel.setColorFilterMode("none") },
                            label = { Text(stringResource(R.string.reader_filter_none)) }
                        )
                        FilterChip(
                            selected = colorFilterMode == "grayscale",
                            onClick = { settingsViewModel.setColorFilterMode("grayscale") },
                            label = { Text(stringResource(R.string.reader_filter_gray)) }
                        )
                        FilterChip(
                            selected = colorFilterMode == "invert",
                            onClick = { settingsViewModel.setColorFilterMode("invert") },
                            label = { Text(stringResource(R.string.reader_filter_invert)) }
                        )
                        FilterChip(
                            selected = colorFilterMode == "sepia",
                            onClick = { settingsViewModel.setColorFilterMode("sepia") },
                            label = { Text(stringResource(R.string.reader_filter_amber)) }
                        )
                    }
                    if (colorFilterMode == "sepia") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.reader_filter_alpha, (colorFilterAlpha * 100).toInt()),
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
            }

            // 4. 辅助开关
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.reader_keep_screen_on),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = keepScreenOn,
                            onCheckedChange = { settingsViewModel.setKeepScreenOn(it) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.reader_transition_anim),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = pageTransitionAnim,
                            onCheckedChange = { settingsViewModel.setPageTransitionAnim(it) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.reader_show_page_number),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = showPersistentPageNumber,
                            onCheckedChange = { settingsViewModel.setShowPersistentPageNumber(it) }
                        )
                    }
                }
            }
        }
    }
}
