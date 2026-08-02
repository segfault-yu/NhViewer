package com.example.nhviewer.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.nhviewer.R
import com.example.nhviewer.ui.theme.NhMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val ThumbWidth = 4.dp
private val ThumbTouchTargetWidth = 24.dp
private val ThumbEndPadding = 4.dp
private val MinThumbHeight = 40.dp
private const val AutoHideDelayMillis = 800L

/** 统一三种 Lazy state 差异的最小接口，供 [FastScrollbarTrack] 复用同一套手势/绘制逻辑。 */
private class FastScrollbarAdapter(
    val firstVisibleItemIndex: () -> Int,
    val totalItemsCount: () -> Int,
    val visibleItemsCount: () -> Int,
    val isScrollInProgress: () -> Boolean,
    val scrollToItem: suspend (Int) -> Unit
)

@Composable
fun FastScrollbar(state: LazyListState, modifier: Modifier = Modifier) {
    val adapter = remember(state) {
        FastScrollbarAdapter(
            firstVisibleItemIndex = { state.firstVisibleItemIndex },
            totalItemsCount = { state.layoutInfo.totalItemsCount },
            visibleItemsCount = { state.layoutInfo.visibleItemsInfo.size },
            isScrollInProgress = { state.isScrollInProgress },
            scrollToItem = { index -> state.scrollToItem(index) }
        )
    }
    FastScrollbarTrack(adapter = adapter, modifier = modifier)
}

@Composable
fun FastScrollbar(state: LazyGridState, modifier: Modifier = Modifier) {
    val adapter = remember(state) {
        FastScrollbarAdapter(
            firstVisibleItemIndex = { state.firstVisibleItemIndex },
            totalItemsCount = { state.layoutInfo.totalItemsCount },
            visibleItemsCount = { state.layoutInfo.visibleItemsInfo.size },
            isScrollInProgress = { state.isScrollInProgress },
            scrollToItem = { index -> state.scrollToItem(index) }
        )
    }
    FastScrollbarTrack(adapter = adapter, modifier = modifier)
}

@Composable
fun FastScrollbar(state: LazyStaggeredGridState, modifier: Modifier = Modifier) {
    val adapter = remember(state) {
        FastScrollbarAdapter(
            firstVisibleItemIndex = { state.firstVisibleItemIndex },
            totalItemsCount = { state.layoutInfo.totalItemsCount },
            visibleItemsCount = { state.layoutInfo.visibleItemsInfo.size },
            isScrollInProgress = { state.isScrollInProgress },
            scrollToItem = { index -> state.scrollToItem(index) }
        )
    }
    FastScrollbarTrack(adapter = adapter, modifier = modifier)
}

@Composable
private fun FastScrollbarTrack(adapter: FastScrollbarAdapter, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dragHandleDescription = stringResource(R.string.common_fast_scrollbar)

    var trackHeightPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var isVisible by remember { mutableStateOf(false) }

    val totalItemsCount = adapter.totalItemsCount()
    val visibleItemsCount = adapter.visibleItemsCount()
    val hasScrollableContent = totalItemsCount > 0 && visibleItemsCount in 1 until totalItemsCount

    val shouldShow = hasScrollableContent && (adapter.isScrollInProgress() || isDragging)
    LaunchedEffect(shouldShow) {
        if (shouldShow) {
            isVisible = true
        } else {
            delay(AutoHideDelayMillis)
            isVisible = false
        }
    }

    if (!hasScrollableContent) return

    fun jumpTo(fraction: Float) {
        val maxIndex = (totalItemsCount - 1).coerceAtLeast(0)
        val targetIndex = (fraction * maxIndex).roundToInt().coerceIn(0, maxIndex)
        coroutineScope.launch { adapter.scrollToItem(targetIndex) }
    }

    val scrollFraction = if (isDragging) {
        dragFraction
    } else {
        val maxIndex = (totalItemsCount - visibleItemsCount).coerceAtLeast(1)
        (adapter.firstVisibleItemIndex().toFloat() / maxIndex).coerceIn(0f, 1f)
    }
    val thumbFraction = (visibleItemsCount.toFloat() / totalItemsCount).coerceIn(0.05f, 1f)
    val minThumbHeightPx = with(density) { MinThumbHeight.roundToPx() }
    val thumbHeightPx = (trackHeightPx * thumbFraction).roundToInt().coerceAtLeast(minThumbHeightPx)
    val thumbOffsetPx = (scrollFraction * (trackHeightPx - thumbHeightPx).coerceAtLeast(0)).roundToInt()

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(ThumbTouchTargetWidth)
            .onGloballyPositioned { trackHeightPx = it.size.height }
            .semantics { contentDescription = dragHandleDescription }
            .pointerInputDragScrub(
                onFractionChanged = { fraction ->
                    dragFraction = fraction
                    jumpTo(fraction)
                },
                onDragStateChanged = { isDragging = it },
                trackHeightPx = { trackHeightPx }
            )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = NhMotion.Effects.default()),
            exit = fadeOut(animationSpec = NhMotion.Effects.default()),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(0, thumbOffsetPx) }
                        .padding(end = ThumbEndPadding)
                        .width(ThumbWidth)
                        .height(with(density) { thumbHeightPx.toDp() })
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            if (isDragging) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                )
            }
        }
    }
}

/** 按下即跳转，随后逐帧跟手；[trackHeightPx] 取实时值以避免捕获到测量前的旧高度。 */
private fun Modifier.pointerInputDragScrub(
    onFractionChanged: (Float) -> Unit,
    onDragStateChanged: (Boolean) -> Unit,
    trackHeightPx: () -> Int
): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            down.consume()
            onDragStateChanged(true)
            val height = trackHeightPx().coerceAtLeast(1)
            onFractionChanged((down.position.y / height).coerceIn(0f, 1f))
            drag(down.id) { change ->
                change.consume()
                val h = trackHeightPx().coerceAtLeast(1)
                onFractionChanged((change.position.y / h).coerceIn(0f, 1f))
            }
            onDragStateChanged(false)
        }
    }
)
