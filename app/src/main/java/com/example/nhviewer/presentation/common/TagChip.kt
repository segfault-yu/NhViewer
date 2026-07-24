package com.example.nhviewer.presentation.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.nhviewer.R
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.i18n.LocalAddToBlacklist
import com.example.nhviewer.util.i18n.LocalBlacklistedTagIds
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.LocalTagLanguage
import com.example.nhviewer.util.i18n.TagTranslationProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCount: Boolean = false,
    tagLanguage: String = LocalTagLanguage.current,
    tagDisplayMode: String = LocalTagDisplayMode.current,
    blacklistedTagIds: Set<Int> = LocalBlacklistedTagIds.current,
    onAddToBlacklist: (Tag) -> Unit = LocalAddToBlacklist.current
) {
    val formattedName = TagTranslationProvider.getFormattedName(tag, tagLanguage, tagDisplayMode)
    val displayText = if (showCount) "$formattedName (${tag.count})" else formattedName

    val isBlacklisted = tag.id in blacklistedTagIds && tag.id != 0
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    // 获取真实的标签类型（处理 female/male 细分）
    val trueType = TagTranslationProvider.getTrueTagType(tag.name, tag.type, tagLanguage).lowercase()

    // 分配固定的 M3 语义化容器与字体色彩
    val (containerColor, labelColor) = if (isBlacklisted) {
        MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    } else when (trueType) {
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

    val shape = RoundedCornerShape(50)

    Box(modifier = modifier) {
        Surface(
            shape = shape,
            color = containerColor,
            contentColor = labelColor,
            modifier = Modifier
                .height(32.dp)
                .clip(shape)
                .then(
                    if (!isBlacklisted) {
                        Modifier.combinedClickable(
                            onClick = onClick,
                            onLongClick = { showMenu = true }
                        )
                    } else {
                        Modifier.alpha(0.5f)
                    }
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.blacklist_add)) },
                onClick = {
                    showMenu = false
                    onAddToBlacklist(tag)
                    Toast.makeText(
                        context,
                        context.getString(R.string.blacklist_added_toast, formattedName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}
