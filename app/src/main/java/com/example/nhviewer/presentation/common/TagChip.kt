package com.example.nhviewer.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhviewer.domain.model.Tag

@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val colors = getColorsForTagType(tag.type, isDark)

    Surface(
        color = colors.first,
        contentColor = colors.second,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = "${tag.name} (${tag.count})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun getColorsForTagType(type: String, isDark: Boolean): Pair<Color, Color> {
    return when (type.lowercase()) {
        "artist" -> {
            if (isDark) {
                Color(0xFF1B5E20) to Color(0xFF81C784)
            } else {
                Color(0xFFE8F5E9) to Color(0xFF2E7D32)
            }
        }
        "language" -> {
            if (isDark) {
                Color(0xFFE65100) to Color(0xFFFFB74D)
            } else {
                Color(0xFFFFF3E0) to Color(0xFFE65100)
            }
        }
        "category" -> {
            if (isDark) {
                Color(0xFF0D47A1) to Color(0xFF90CAF9)
            } else {
                Color(0xFFE3F2FD) to Color(0xFF1565C0)
            }
        }
        "parody" -> {
            if (isDark) {
                Color(0xFF4A148C) to Color(0xFFCE93D8)
            } else {
                Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
            }
        }
        "character" -> {
            if (isDark) {
                Color(0xFF880E4F) to Color(0xFFF48FB1)
            } else {
                Color(0xFFFCE4EC) to Color(0xFFC2185B)
            }
        }
        "group" -> {
            if (isDark) {
                Color(0xFF004D40) to Color(0xFF80CBC4)
            } else {
                Color(0xFFE0F2F1) to Color(0xFF00695C)
            }
        }
        else -> {
            // Default "tag"
            if (isDark) {
                Color(0xFF37474F) to Color(0xFFECEFF1)
            } else {
                Color(0xFFECEFF1) to Color(0xFF37474F)
            }
        }
    }
}
