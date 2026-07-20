package com.example.nhviewer.presentation.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.LocalTagLanguage
import com.example.nhviewer.util.i18n.TagTranslationProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCount: Boolean = false,
    tagLanguage: String = LocalTagLanguage.current,
    tagDisplayMode: String = LocalTagDisplayMode.current
) {
    val formattedName = TagTranslationProvider.getFormattedName(tag, tagLanguage, tagDisplayMode)
    val displayText = if (showCount) "$formattedName (${tag.count})" else formattedName

    // 分配固定的 M3 语义化容器与字体色彩
    val (containerColor, labelColor) = when (tag.type.lowercase()) {
        "language" -> {
            MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.primary
        }
        "parody" -> {
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        }
        "character" -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
        "artist" -> {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
        "group" -> {
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
        "female" -> {
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        }
        "male" -> {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }
        "category" -> {
            when (tag.name.lowercase()) {
                "doujinshi", "同人志" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                "manga", "漫画" -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
                "artistcg", "artist cg", "画集" -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
                "gamecg", "game cg", "游戏 cg" -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
                "western", "欧美" -> MaterialTheme.colorScheme.inverseSurface to MaterialTheme.colorScheme.inverseOnSurface
                "non-h", "非 h" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                "imageset", "图集" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                "cosplay" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
            }
        }
        else -> { // "tag" / "other"
            MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurface
        }
    }

    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        shape = RoundedCornerShape(50),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        ),
        border = null,
        modifier = modifier
    )
}
