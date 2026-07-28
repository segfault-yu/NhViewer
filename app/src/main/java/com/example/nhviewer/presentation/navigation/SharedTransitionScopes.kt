package com.example.nhviewer.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

/**
 * 共享元素转场所需的两个 Scope，通过 CompositionLocal 下发，避免给每个 Screen 加参数。
 * 任一为 null 时（例如组件被用在 NavHost 之外的上下文），调用方应跳过 sharedElement 修饰符。
 */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/** 画廊封面图的共享元素：列表卡片与详情页封面用同一个 `itemId` 时会连续变形过渡。 */
@Composable
fun Modifier.galleryCoverSharedElement(itemId: Int): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    return with(sharedTransitionScope) {
        sharedElement(
            sharedTransitionScope.rememberSharedContentState(key = "cover-$itemId"),
            animatedVisibilityScope
        )
    }
}

/** 卡片容器的共享边界：列表卡（Row/Column）与详情页封面容器结构不同，用 sharedBounds 而非 sharedElement。 */
@Composable
fun Modifier.galleryCardSharedBounds(itemId: Int): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    return with(sharedTransitionScope) {
        sharedBounds(
            sharedTransitionScope.rememberSharedContentState(key = "card-$itemId"),
            animatedVisibilityScope
        )
    }
}

/** 供共享边界内的标题等文本使用，防止转场过程中因容器尺寸插值导致的文字重排抖动。 */
@Composable
fun Modifier.gallerySkipToLookaheadSize(): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    return with(sharedTransitionScope) { skipToLookaheadSize() }
}
