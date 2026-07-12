package com.example.nhviewer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val NhViewerDarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = OnDarkPrimary,
    primaryContainer = RosePrimaryDim,
    onPrimaryContainer = OnDarkPrimary,
    secondary = RosePrimary,
    onSecondary = OnDarkPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = OnDarkSurface,
    tertiary = RosePrimaryDark,
    onTertiary = OnDarkPrimary,
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    outline = DarkOutline,
    error = ErrorDark,
    onError = DarkBackground
)

private val NhViewerLightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = OnDarkPrimary,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = RosePrimaryDim,
    secondary = RosePrimaryDim,
    onSecondary = OnDarkPrimary,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = RosePrimaryDim,
    tertiary = RosePrimary,
    onTertiary = OnDarkPrimary,
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = DarkSurfaceVariant,
    outline = LightOutline,
    error = ErrorColor,
    onError = OnDarkPrimary
)

/** 是否处于暗色模式的 CompositionLocal，便于子组件读取 */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun NhViewerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NhViewerDarkColorScheme else NhViewerLightColorScheme

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NhViewerTypography,
            content = content
        )
    }
}