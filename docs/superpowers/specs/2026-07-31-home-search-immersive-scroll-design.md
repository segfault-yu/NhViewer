# 首页/搜索结果沉浸式滚动隐藏 —— 设计文档

日期：2026-07-31

## 背景

首页（`HomeScreen.kt`）常驻顶部搜索栏、"最新/热门" tab 栏，以及搜索结果态下的搜索栏 + 筛选芯片 + 排序栏，长期占用屏幕空间。用户希望在浏览列表内容时能有沉浸式体验：向上滑动浏览更多内容时自动隐藏这些工具区，向下滑回顶部时自动重新显示。

## 范围

仅覆盖：
- 首页默认态（`!active`）：顶部搜索栏 Box + `PrimaryTabRow`（最新/热门）
- 首页内嵌的搜索结果态（`!active && searchQuery.isNotEmpty()`）：顶部搜索栏 Box + `SearchQueryBuilder` 筛选芯片行 + `SearchSortBar` 排序栏
- 右下角骰子随机 FAB

不覆盖：详情页、阅读器、标签页、收藏/历史等独立页面；搜索展开态（`active == true`，输入框获得焦点时的历史/建议面板，不含长列表滚动场景）。

## 方向语义

采用行业惯例：手指向上滑（内容向下滚，用户在向后浏览更多内容）→ 隐藏工具区；手指向下滑（内容向上回滚，用户在往回看）→ 显示工具区。

## 架构与状态

在 `HomeScreen.kt` 顶层新增：

```kotlin
var isChromeVisible by remember { mutableStateOf(true) }
val hideThresholdPx = with(LocalDensity.current) { 8.dp.toPx() }
val chromeScrollConnection = remember(hideThresholdPx) {
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            when {
                available.y < -hideThresholdPx -> isChromeVisible = false
                available.y > hideThresholdPx -> isChromeVisible = true
            }
            return Offset.Zero
        }
    }
}
```

三个列表共用同一个 `isChromeVisible` / `chromeScrollConnection`（同一时刻只有一个列表可见）：
- "最新" tab 的 `LazyVerticalStaggeredGrid`
- "热门" tab 的 `LazyVerticalGrid`
- 搜索结果 `SearchResultGrid` 内部的 `LazyVerticalStaggeredGrid`

`SearchResultGrid`（`presentation/common/SearchContent.kt`）目前没有对外暴露挂载 `nestedScroll` 的入口，需要新增一个可选参数（例如 `scrollConnection: NestedScrollConnection? = null`），在内部把它 `.then(...)` 到网格的 `Modifier` 上。这是唯一需要改动现有公共组件签名的地方。

## 受控 UI 区域与动画

- **首页默认态**：顶部搜索栏 Box + `PrimaryTabRow` 一起包进 `AnimatedVisibility(visible = isChromeVisible, enter = ..., exit = ...)`，滑动方向为向上收起/向下展开（`slideOutVertically { -it } + fadeOut` / `slideInVertically { -it } + fadeIn`）。
- **搜索结果态**：搜索栏 Box + `SearchQueryBuilder` + `SearchSortBar` 同样整体包进一个 `AnimatedVisibility(visible = isChromeVisible)`，动效同上。
- **骰子 FAB**：`Scaffold` 的 `floatingActionButton` 显示条件从 `if (!active)` 改为 `if (!active && isChromeVisible)`，外层包 `AnimatedVisibility`，滑动方向相反（向下滑出/向上滑入），即工具栏往上收的同时悬浮球往下沉。
- 动效时长/曲线复用现有 `NhMotion.Spatial.default()` / `NhMotion.Effects.default()`，不新增动效参数体系。

## 边界情况

1. **滚动死区**：单次滚动增量的正负号决定方向，设置一个约 8dp 的死区阈值（`onPreScroll` 的 `available.y` 是像素单位，实现时用 `LocalDensity` 把 8dp 换算成 px 再比较），过滤微小滚动抖动导致的闪烁。
2. **列表顶部兜底**：任一列表滚动回到最顶部时，无条件把 `isChromeVisible` 重置为 `true`，避免"已经回到顶部但工具栏因为最后一次手势方向而不出现"的别扭体验。`LazyGridState` 用 `firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0` 判断；`LazyStaggeredGridState` 侧用其对应的等价状态（具体 API 在实现阶段确认），语义一致。
3. **切换 tab / 清空搜索关键词**：切换"最新"↔"热门"，或搜索关键词被清空回到首页默认态时，重置 `isChromeVisible = true`，新列表默认展示完整工具区。
4. **内容不足一屏**：列表本身不产生滚动时，`isChromeVisible` 保持初始值 `true`，不存在因缺乏滚动事件而无法再次显示的死锁。

## 验证方式

不新增自动化测试（项目里没有类似的手势驱动交互测试先例，写一个脆弱的手势模拟测试收益低于成本）。改为在真机/模拟器上用 adb 手动跑一遍：

- "最新" / "热门" / 搜索结果三个列表分别向上滑触发隐藏、向下滑触发显示
- 滚动到列表顶部，确认工具栏强制恢复显示
- 切换"最新"↔"热门"、清空搜索关键词，确认工具栏重新显示
- 骰子按钮的隐藏/显示节奏与顶部工具区一致
