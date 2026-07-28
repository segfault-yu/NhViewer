package com.example.nhviewer.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry

object NhMotion {

    /** 位移/尺寸类动效（形状或边界变化）。 */
    object Spatial {
        fun <T> default(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 380f)
        fun <T> fast(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.6f, stiffness = 800f)
        fun <T> slow(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 200f)
    }

    /** 透明度/颜色类动效（无回弹）。 */
    object Effects {
        fun <T> default(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 1600f)
        fun <T> fast(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 3800f)
        fun <T> slow(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 800f)
    }

    /**
     * 预见式返回退出页的目标缩放比例（Material 规范值）。
     * 由 popExit() 直接嵌入，随手势进度自动插值（详见 docs/motion-api-baseline.md 第 4 节）。
     */
    const val PredictiveBackMinScale = 0.9f

    /** NavHost 前进导航：新页从右侧滑入。 */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.enterSlide(): EnterTransition =
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = Spatial.default()
        ) + fadeIn(animationSpec = Effects.default())

    /** NavHost 前进导航：旧页向左侧滑出。 */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.exitSlide(): ExitTransition =
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = Spatial.default()
        ) + fadeOut(animationSpec = Effects.default())

    /** NavHost 返回导航（含预见式返回手势）：旧页从左侧滑回。 */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter(): EnterTransition =
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = Spatial.default()
        ) + fadeIn(animationSpec = Effects.default())

    /**
     * NavHost 返回导航（含预见式返回手势）：当前页缩小并淡出。
     * 缩放目标值 [PredictiveBackMinScale] 会被 SeekableTransitionState 按手势进度自动插值，
     * 无需业务代码手动读取 backEvent.progress。
     */
    fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
        scaleOut(targetScale = PredictiveBackMinScale, animationSpec = Spatial.default()) +
            fadeOut(animationSpec = Effects.default())
}
