package com.example.nhviewer.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nhviewer.domain.model.Tag

@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCount: Boolean = false
) {
    // 依据用户决策 B，将标签类型缩减映射到 3 类 M3 标准语义色容器上
    val (containerColor, labelColor) = when (tag.type.lowercase()) {
        "artist", "character" -> {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
        "parody", "group" -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
        else -> { // "language", "category", "tag" 等
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        }
    }

    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = if (showCount) "${tag.name} (${tag.count})" else tag.name,
                style = MaterialTheme.typography.labelMedium
            )
        },
        shape = MaterialTheme.shapes.small,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        ),
        border = null,
        modifier = modifier
    )
}
