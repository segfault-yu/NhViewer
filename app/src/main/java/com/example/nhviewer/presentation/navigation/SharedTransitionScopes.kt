package com.example.nhviewer.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape

/**
 * 共享元素转场所需的两个 Scope，通过 CompositionLocal 下发，避免给每个 Screen 加参数。
 * 任一为 null 时（例如组件被用在 NavHost 之外的上下文），调用方应跳过 sharedElement 修饰符。
 */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * 画廊封面图的共享元素：列表卡片与详情页封面用同一个 `itemId` 时会连续变形过渡。
 * 转场进行时该元素会被渲染进 SharedTransitionScope 的 overlay 层，脱离父容器裁剪；
 * 官方文档要求裁剪必须作为 sharedElement 的子修饰符（即写在其后）才能在 overlay 中生效，
 * 否则动画过程中会先出现直角、动画结束落回父容器裁剪后才变回圆角。
 */
@Composable
fun Modifier.galleryCoverSharedElement(itemId: Int, shape: Shape = MaterialTheme.shapes.medium): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this.clip(shape)
    return with(sharedTransitionScope) {
        sharedElement(
            sharedTransitionScope.rememberSharedContentState(key = "cover-$itemId"),
            animatedVisibilityScope
        )
    }.clip(shape)
}

/**
 * 卡片容器的共享边界：列表卡（Row/Column）与详情页封面容器结构不同，用 sharedBounds 而非 sharedElement。
 * 卡片自身的圆角裁剪由内部 ElevatedCard/Surface 实现，位于 sharedBounds 修饰符之后（子层级），
 * 无法像封面图那样简单追加 clip；显式传入 clipInOverlayDuringTransition 让 overlay 渲染阶段
 * 同样按目标圆角裁剪，避免转场期间卡片边缘闪现直角。
 */
@Composable
fun Modifier.galleryCardSharedBounds(itemId: Int, shape: Shape = MaterialTheme.shapes.medium): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    return with(sharedTransitionScope) {
        sharedBounds(
            sharedTransitionScope.rememberSharedContentState(key = "card-$itemId"),
            animatedVisibilityScope,
            clipInOverlayDuringTransition = OverlayClip(shape)
        )
    }
}

/** 供共享边界内的标题等文本使用，防止转场过程中因容器尺寸插值导致的文字重排抖动。 */
@Composable
fun Modifier.gallerySkipToLookaheadSize(): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    return with(sharedTransitionScope) { skipToLookaheadSize() }
}
