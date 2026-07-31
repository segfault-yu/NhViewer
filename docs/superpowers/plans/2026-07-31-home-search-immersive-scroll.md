# 首页/搜索结果沉浸式滚动隐藏 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 首页（最新/热门列表、内嵌搜索结果列表）在向上滑动浏览更多内容时自动隐藏顶部搜索栏/tab 栏/筛选排序栏和右下角骰子悬浮球，向下滑动或滚回列表顶部时自动恢复显示。

**Architecture:** 在 `HomeScreen.kt` 顶层维护一个共享的 `isChromeVisible` 状态和一个 `NestedScrollConnection`，三个可滚动列表（最新/热门/搜索结果）都挂上同一个连接来驱动这个状态；顶部工具区、tab 栏、筛选排序栏、悬浮球分别用 `AnimatedVisibility` 包裹并读取这个状态。`SearchResultGrid`（`SearchContent.kt`）需要把内部网格状态和这个连接接受为可选外部参数才能被 `HomeScreen` 接入。

**Tech Stack:** Jetpack Compose（Material3 + Foundation Lazy Grid/StaggeredGrid + `NestedScrollConnection`），Kotlin，无新增第三方依赖。

## Global Constraints

- 仅覆盖首页默认态（最新/热门列表）与首页内嵌搜索结果态；不改动详情页、阅读器、标签页、收藏/历史等独立页面，也不改动搜索展开态（`active == true` 的历史/建议面板）。
- 方向语义：手指向上滑（内容向下滚，浏览更多）→隐藏；手指向下滑（回看）→显示。
- 骰子 FAB 和顶部工具区用同一个 `isChromeVisible` 状态同步隐藏/显示。
- 滚动死区固定为 8dp（换算成 px 后比较），避免微小滚动抖动导致闪烁。
- 任一列表滚回最顶部（`firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0`）时强制 `isChromeVisible = true`。
- 切换"最新/热门" tab、或进入搜索结果态时，重置 `isChromeVisible = true`。
- 动效复用现有 `NhMotion.Spatial.default()` / `NhMotion.Effects.default()`，不新增动效参数体系。
- 不新增自动化测试，改为用 adb 在真机/模拟器上手动验证（已连接设备：`2205163c`，包名 `com.example.nhviewer`）。
- 每个任务完成后用 `./gradlew.bat :app:installDebug` 编译安装，再用 adb 手动验证，最后提交一次 commit。

---

### Task 1: 共享滚动状态 + "最新" 列表接入

**Files:**
- Modify: `app/src/main/java/com/example/nhviewer/presentation/feature/home/HomeScreen.kt`

**Interfaces:**
- Produces：`isChromeVisible: MutableState<Boolean>`（在 `HomeScreen` 函数体内，通过 `remember` 声明的局部变量）、`chromeScrollConnection: NestedScrollConnection`（同样是局部 `remember` 值）。后续任务（2、3）直接复用这两个同名局部变量，不需要额外传参，因为它们和后续任务修改的代码在同一个函数体内。

- [ ] **Step 1: 新增所需 import**

在 `app/src/main/java/com/example/nhviewer/presentation/feature/home/HomeScreen.kt` 里做以下四处 import 修改。

修改一：

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
```
替换为：
```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
```

修改二：

```kotlin
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
```
替换为：
```kotlin
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
```

修改三：

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
```
替换为：
```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
```

修改四：

```kotlin
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
```
替换为：
```kotlin
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
```

- [ ] **Step 2: 声明共享的沉浸式滚动状态**

找到：
```kotlin
    val searchTrigger by searchViewModel.searchTrigger.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
```
替换为：
```kotlin
    val searchTrigger by searchViewModel.searchTrigger.collectAsState()

    // 沉浸式滚动：向上滑（浏览更多内容）隐藏顶部工具区与悬浮球，向下滑或回到列表顶部时恢复显示
    var isChromeVisible by remember { mutableStateOf(true) }
    val chromeHideThresholdPx = with(LocalDensity.current) { 8.dp.toPx() }
    val chromeScrollConnection = remember(chromeHideThresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                when {
                    available.y < -chromeHideThresholdPx -> isChromeVisible = false
                    available.y > chromeHideThresholdPx -> isChromeVisible = true
                }
                return Offset.Zero
            }
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
```

- [ ] **Step 3: 顶部搜索栏 Box 包进 `AnimatedVisibility`**

找到：
```kotlin
            // 顶部常驻 SearchBar，融合“我的”入口
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (active) {
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        } else {
                            Modifier.statusBarsPadding()
                        }
                    )
                    .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp)
            ) {
                NhSearchBar(
```
替换为：
```kotlin
            // 顶部常驻 SearchBar，融合“我的”入口，沉浸式滚动时随 isChromeVisible 一起隐藏/显示
            AnimatedVisibility(
                visible = isChromeVisible,
                enter = slideInVertically(animationSpec = NhMotion.Spatial.default()) { -it } +
                    fadeIn(animationSpec = NhMotion.Effects.default()),
                exit = slideOutVertically(animationSpec = NhMotion.Spatial.default()) { -it } +
                    fadeOut(animationSpec = NhMotion.Effects.default())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (active) {
                                Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            } else {
                                Modifier.statusBarsPadding()
                            }
                        )
                        .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp)
                ) {
                    NhSearchBar(
```

紧接着这段的闭合大括号也要多加一层。找到（`NhSearchBar(...)` 调用结尾处）：
```kotlin
                    }
                )
            }

            // 搜索结果页与普通主页内容区域动态切换
```
替换为：
```kotlin
                    }
                    )
                }
            }

            // 搜索结果页与普通主页内容区域动态切换
```

- [ ] **Step 4: 骰子 FAB 包进 `AnimatedVisibility`**

找到：
```kotlin
    Scaffold(
        floatingActionButton = {
            if (!active) {
                FloatingActionButton(
                    onClick = { viewModel.playRandom() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Random"
                    )
                }
            }
        },
```
替换为：
```kotlin
    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = !active && isChromeVisible,
                enter = slideInVertically(animationSpec = NhMotion.Spatial.default()) { it } +
                    fadeIn(animationSpec = NhMotion.Effects.default()),
                exit = slideOutVertically(animationSpec = NhMotion.Spatial.default()) { it } +
                    fadeOut(animationSpec = NhMotion.Effects.default())
            ) {
                FloatingActionButton(
                    onClick = { viewModel.playRandom() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Random"
                    )
                }
            }
        },
```

- [ ] **Step 5: "最新" 列表接入滚动状态**

找到：
```kotlin
                        0 -> {
                            val pullRefreshState = rememberPullToRefreshState()
                            var isRefreshing by remember { mutableStateOf(false) }
                            LaunchedEffect(latestGalleries.loadState.refresh) {
                                isRefreshing = latestGalleries.loadState.refresh is LoadState.Loading
                            }

                            PullToRefreshBox(
```
替换为：
```kotlin
                        0 -> {
                            val pullRefreshState = rememberPullToRefreshState()
                            var isRefreshing by remember { mutableStateOf(false) }
                            val latestGridState = rememberLazyStaggeredGridState()
                            LaunchedEffect(latestGalleries.loadState.refresh) {
                                isRefreshing = latestGalleries.loadState.refresh is LoadState.Loading
                            }
                            LaunchedEffect(latestGridState) {
                                snapshotFlow {
                                    latestGridState.firstVisibleItemIndex to latestGridState.firstVisibleItemScrollOffset
                                }.collect { (index, offset) ->
                                    if (index == 0 && offset == 0) isChromeVisible = true
                                }
                            }

                            PullToRefreshBox(
```

再找到：
```kotlin
                                    LazyVerticalStaggeredGrid(
                                        columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                                        contentPadding = PaddingValues(
                                            start = 12.dp,
                                            top = 12.dp,
                                            end = 12.dp,
                                            bottom = 12.dp + innerPadding.calculateBottomPadding()
                                        ),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            count = latestGalleries.itemCount,
```
替换为：
```kotlin
                                    LazyVerticalStaggeredGrid(
                                        columns = StaggeredGridCells.Adaptive(minSize = 340.dp),
                                        state = latestGridState,
                                        contentPadding = PaddingValues(
                                            start = 12.dp,
                                            top = 12.dp,
                                            end = 12.dp,
                                            bottom = 12.dp + innerPadding.calculateBottomPadding()
                                        ),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .nestedScroll(chromeScrollConnection)
                                    ) {
                                        items(
                                            count = latestGalleries.itemCount,
```

- [ ] **Step 6: 编译安装**

```bash
cd F:/dev/nhentai && ./gradlew.bat :app:installDebug --console=plain
```
预期：`BUILD SUCCESSFUL`，且 `Installed on 1 device.`

- [ ] **Step 7: adb 手动验证**

```bash
adb shell am force-stop com.example.nhviewer && adb shell am start -n com.example.nhviewer/.MainActivity
```
等 App 打开、停在"最新" tab（默认 tab），依次执行：

```bash
adb shell input swipe 450 1800 450 400 300
```
预期：截图（`adb exec-out screencap -p > out.png`）显示顶部搜索栏 + "最新/热门" tab 栏 + 右下角骰子按钮都已滑出隐藏。

```bash
adb shell input swipe 450 400 450 1800 300
```
预期：截图显示三者都已恢复显示。

```bash
adb shell input swipe 450 1800 450 400 300
adb shell input swipe 450 1800 450 400 300
adb shell input swipe 450 1800 450 400 300
adb shell input swipe 450 1800 450 400 300
adb shell input swipe 450 1800 450 400 300
```
（连续多次向上滑，确保滚动到列表顶部之外的区域后再滚回顶部）之后：
```bash
adb shell input swipe 450 400 450 2200 100
adb shell input swipe 450 400 450 2200 100
adb shell input swipe 450 400 450 2200 100
```
预期：即使最后一次手势仍在向"隐藏"方向切换的临界点，只要列表真正回到顶部，工具栏和骰子按钮都会强制恢复显示（用截图确认列表第一项已经完整可见）。

- [ ] **Step 8: Commit**

```bash
cd F:/dev/nhentai
git add app/src/main/java/com/example/nhviewer/presentation/feature/home/HomeScreen.kt
git commit -m "feat: 首页顶部工具区随「最新」列表滚动方向沉浸式隐藏/显示"
```

---

### Task 2: "热门" 列表接入 + tab 切换重置

**Files:**
- Modify: `app/src/main/java/com/example/nhviewer/presentation/feature/home/HomeScreen.kt`

**Interfaces:**
- Consumes：Task 1 产出的 `isChromeVisible`（局部变量）、`chromeScrollConnection`（局部变量），二者在同一函数体内直接可见，无需额外传参。

- [ ] **Step 1: tab 切换时重置显示状态**

找到：
```kotlin
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.home_tab_latest),
        stringResource(R.string.home_tab_popular)
    )
```
替换为：
```kotlin
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.home_tab_latest),
        stringResource(R.string.home_tab_popular)
    )

    // 切换"最新/热门" tab 时重新显示工具区，不带上一个列表滚动到一半的隐藏状态
    LaunchedEffect(selectedTabIndex) {
        isChromeVisible = true
    }
```

- [ ] **Step 2: "热门" 列表接入滚动状态**

找到：
```kotlin
                        1 -> {
                            val pullRefreshState = rememberPullToRefreshState()
                            val isRefreshing = popularState is HomeViewModel.PopularState.Loading

                            PullToRefreshBox(
```
替换为：
```kotlin
                        1 -> {
                            val pullRefreshState = rememberPullToRefreshState()
                            val isRefreshing = popularState is HomeViewModel.PopularState.Loading
                            val popularGridState = rememberLazyGridState()
                            LaunchedEffect(popularGridState) {
                                snapshotFlow {
                                    popularGridState.firstVisibleItemIndex to popularGridState.firstVisibleItemScrollOffset
                                }.collect { (index, offset) ->
                                    if (index == 0 && offset == 0) isChromeVisible = true
                                }
                            }

                            PullToRefreshBox(
```

再找到：
```kotlin
                                        LazyVerticalGrid(
                                            columns = GridCells.Adaptive(minSize = 340.dp),
                                            contentPadding = PaddingValues(
                                                start = 12.dp,
                                                top = 12.dp,
                                                end = 12.dp,
                                                bottom = 12.dp + innerPadding.calculateBottomPadding()
                                            ),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(
                                                items = state.items,
```
替换为：
```kotlin
                                        LazyVerticalGrid(
                                            columns = GridCells.Adaptive(minSize = 340.dp),
                                            state = popularGridState,
                                            contentPadding = PaddingValues(
                                                start = 12.dp,
                                                top = 12.dp,
                                                end = 12.dp,
                                                bottom = 12.dp + innerPadding.calculateBottomPadding()
                                            ),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .nestedScroll(chromeScrollConnection)
                                        ) {
                                            items(
                                                items = state.items,
```

- [ ] **Step 3: 编译安装**

```bash
cd F:/dev/nhentai && ./gradlew.bat :app:installDebug --console=plain
```
预期：`BUILD SUCCESSFUL`。

- [ ] **Step 4: adb 手动验证**

```bash
adb shell am force-stop com.example.nhviewer && adb shell am start -n com.example.nhviewer/.MainActivity
```
点击"热门" tab（坐标视当前分辨率而定，可先截图确认按钮位置），然后：

```bash
adb shell input swipe 450 1800 450 400 300
```
预期：截图显示"热门" tab 下顶部工具区和骰子按钮同样能隐藏。

```bash
adb shell input swipe 450 400 450 1800 300
```
预期：恢复显示。

在"热门"列表保持隐藏状态时，点击切回"最新" tab，截图确认工具区立刻恢复显示（不需要额外滑动）。

- [ ] **Step 5: Commit**

```bash
cd F:/dev/nhentai
git add app/src/main/java/com/example/nhviewer/presentation/feature/home/HomeScreen.kt
git commit -m "feat: 「热门」列表接入沉浸式滚动，tab 切换时重置工具区显示状态"
```

---

### Task 3: 搜索结果列表接入 + 筛选/排序栏沉浸式隐藏

**Files:**
- Modify: `app/src/main/java/com/example/nhviewer/presentation/common/SearchContent.kt`
- Modify: `app/src/main/java/com/example/nhviewer/presentation/feature/home/HomeScreen.kt`

**Interfaces:**
- `SearchResultGrid` 新增两个可选参数（均有默认值，向后兼容现有调用方 `SearchScreen.kt`）：
  - `gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState()`
  - `scrollConnection: NestedScrollConnection? = null`
- Consumes：Task 1 产出的 `isChromeVisible`、`chromeScrollConnection`（`HomeScreen.kt` 内局部变量）。

- [ ] **Step 1: `SearchContent.kt` 新增 import**

找到：
```kotlin
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
```
替换为：
```kotlin
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
```

找到：
```kotlin
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
```
替换为：
```kotlin
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
```

- [ ] **Step 2: `SearchResultGrid` 签名新增 `gridState`/`scrollConnection` 参数**

找到：
```kotlin
@Composable
fun SearchResultGrid(
    searchResults: LazyPagingItems<GalleryListItem>,
    cdnHost: String,
    favoritedIds: Set<Int>,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minSize: Int = 340,
    bottomPadding: Dp = 12.dp,
    scrollToTopKey: Any? = null
) {
    val gridState = rememberLazyStaggeredGridState()

    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey != null) {
            gridState.animateScrollToItem(0)
        }
    }
```
替换为：
```kotlin
@Composable
fun SearchResultGrid(
    searchResults: LazyPagingItems<GalleryListItem>,
    cdnHost: String,
    favoritedIds: Set<Int>,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minSize: Int = 340,
    bottomPadding: Dp = 12.dp,
    scrollToTopKey: Any? = null,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    scrollConnection: NestedScrollConnection? = null
) {
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey != null) {
            gridState.animateScrollToItem(0)
        }
    }
```

- [ ] **Step 3: 网格 `Modifier` 挂载可选的 `scrollConnection`**

找到：
```kotlin
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = minSize.dp),
                        state = gridState,
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            top = 12.dp,
                            end = 12.dp,
                            bottom = bottomPadding
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
```
替换为：
```kotlin
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = minSize.dp),
                        state = gridState,
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            top = 12.dp,
                            end = 12.dp,
                            bottom = bottomPadding
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (scrollConnection != null) {
                                    Modifier.nestedScroll(scrollConnection)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
```

- [ ] **Step 4: 编译检查（仅这一个文件的改动）**

```bash
cd F:/dev/nhentai && ./gradlew.bat :app:compileDebugKotlin --console=plain
```
预期：`BUILD SUCCESSFUL`（此时 `HomeScreen.kt` 还没接入新参数，`SearchScreen.kt` 的旧调用点因为两个新参数都有默认值，应保持可编译）。

- [ ] **Step 5: `HomeScreen.kt` 接入筛选/排序栏沉浸式隐藏**

找到：
```kotlin
            // 搜索结果页与普通主页内容区域动态切换
            if (!active && searchQuery.isNotEmpty()) {
                SearchQueryBuilder(
                    rawQuery = searchQuery,
                    onQueryChanged = {
                        searchViewModel.onSearchQueryChanged(it)
                        // 文本改变时不触发搜索，SearchQueryBuilder 内部的选择操作会通过 onTriggerSearch 触发
                    },
                    onTriggerSearch = { query ->
                        searchViewModel.search(query)
                        focusManager.clearFocus()
                    }
                )
                AnimatedVisibility(
                    visible = searchResults.itemCount > 0,
                    enter = fadeIn(animationSpec = NhMotion.Effects.default()),
                    exit = fadeOut(animationSpec = NhMotion.Effects.default())
                ) {
                    SearchSortBar(
                        sortOption = sortOption,
                        onSortOptionChanged = { searchViewModel.onSortOptionChanged(it) },
                        totalCount = totalResults
                    )
                }

                SearchResultGrid(
                    searchResults = searchResults,
                    cdnHost = cdnHost,
                    favoritedIds = favoritedIds,
                    onNavigateToDetail = onNavigateToDetail,
                    minSize = 340,
                    bottomPadding = 12.dp + innerPadding.calculateBottomPadding(),
                    scrollToTopKey = searchTrigger
                )
            } else if (!active) {
```
替换为：
```kotlin
            // 搜索结果页与普通主页内容区域动态切换
            if (!active && searchQuery.isNotEmpty()) {
                val searchResultGridState = rememberLazyStaggeredGridState()
                // 进入搜索结果态时重置显示状态（同时覆盖了清空关键词后再次搜索的情况）
                LaunchedEffect(Unit) {
                    isChromeVisible = true
                }
                LaunchedEffect(searchResultGridState) {
                    snapshotFlow {
                        searchResultGridState.firstVisibleItemIndex to searchResultGridState.firstVisibleItemScrollOffset
                    }.collect { (index, offset) ->
                        if (index == 0 && offset == 0) isChromeVisible = true
                    }
                }

                AnimatedVisibility(
                    visible = isChromeVisible,
                    enter = slideInVertically(animationSpec = NhMotion.Spatial.default()) { -it } +
                        fadeIn(animationSpec = NhMotion.Effects.default()),
                    exit = slideOutVertically(animationSpec = NhMotion.Spatial.default()) { -it } +
                        fadeOut(animationSpec = NhMotion.Effects.default())
                ) {
                    Column {
                        SearchQueryBuilder(
                            rawQuery = searchQuery,
                            onQueryChanged = {
                                searchViewModel.onSearchQueryChanged(it)
                                // 文本改变时不触发搜索，SearchQueryBuilder 内部的选择操作会通过 onTriggerSearch 触发
                            },
                            onTriggerSearch = { query ->
                                searchViewModel.search(query)
                                focusManager.clearFocus()
                            }
                        )
                        AnimatedVisibility(
                            visible = searchResults.itemCount > 0,
                            enter = fadeIn(animationSpec = NhMotion.Effects.default()),
                            exit = fadeOut(animationSpec = NhMotion.Effects.default())
                        ) {
                            SearchSortBar(
                                sortOption = sortOption,
                                onSortOptionChanged = { searchViewModel.onSortOptionChanged(it) },
                                totalCount = totalResults
                            )
                        }
                    }
                }

                SearchResultGrid(
                    searchResults = searchResults,
                    cdnHost = cdnHost,
                    favoritedIds = favoritedIds,
                    onNavigateToDetail = onNavigateToDetail,
                    minSize = 340,
                    bottomPadding = 12.dp + innerPadding.calculateBottomPadding(),
                    scrollToTopKey = searchTrigger,
                    gridState = searchResultGridState,
                    scrollConnection = chromeScrollConnection
                )
            } else if (!active) {
```

- [ ] **Step 6: 编译安装**

```bash
cd F:/dev/nhentai && ./gradlew.bat :app:installDebug --console=plain
```
预期：`BUILD SUCCESSFUL`。

- [ ] **Step 7: adb 手动验证**

```bash
adb shell am force-stop com.example.nhviewer && adb shell am start -n com.example.nhviewer/.MainActivity
adb shell input tap 540 218
adb shell input text "control"
adb shell input keyevent 66
adb shell input tap 990 218
```
（搜索出结果后）：
```bash
adb shell input swipe 450 1800 450 400 300
```
预期：截图显示搜索栏 + 筛选芯片行 + 排序栏一起隐藏，下面的结果网格保持显示不受影响。

```bash
adb shell input swipe 450 400 450 1800 300
```
预期：三者恢复显示。

隐藏状态下点击搜索框右侧的清除按钮清空关键词，回到首页默认态，截图确认顶部工具区（搜索栏 + tab 栏）正常显示（验证"进入/离开搜索结果态重置显示状态"）。

- [ ] **Step 8: Commit**

```bash
cd F:/dev/nhentai
git add app/src/main/java/com/example/nhviewer/presentation/common/SearchContent.kt
git add app/src/main/java/com/example/nhviewer/presentation/feature/home/HomeScreen.kt
git commit -m "feat: 搜索结果的筛选/排序栏接入沉浸式滚动隐藏"
```

---

## Self-Review Notes

- **Spec 覆盖**：方向语义（Task 1 `onPreScroll` 判断） / 范围（仅 Task 1-3 涉及的 Home + 内嵌搜索结果，未触碰其他页面文件） / FAB 跟随（Task 1 Step 4） / 滚动死区 8dp（Task 1 Step 2） / 顶部兜底（Task 1 Step 5、Task 2 Step 2、Task 3 Step 5 三处 `snapshotFlow`） / tab 切换与进入搜索结果态重置（Task 2 Step 1、Task 3 Step 5）——spec 里的每一条都能对应到具体 task，没有遗漏。
- **占位符扫描**：全文没有 TBD/TODO，每个 diff 都是完整可直接套用的代码块。
- **类型一致性**：`isChromeVisible`（`Boolean`）、`chromeScrollConnection`（`NestedScrollConnection`）在三个任务里引用的都是 Task 1 声明的同一个局部变量名，没有改名或类型不一致的问题；`SearchResultGrid` 新增的 `gridState: LazyStaggeredGridState` / `scrollConnection: NestedScrollConnection?` 与 `HomeScreen.kt` 里调用处传入的 `searchResultGridState` / `chromeScrollConnection` 类型完全匹配。
- **API 已核实**：`LazyStaggeredGridState.firstVisibleItemIndex` / `firstVisibleItemScrollOffset`、`LazyGridState` 同名属性、`NestedScrollConnection`/`NestedScrollSource` 包路径均已用 `javap` 对本机 Gradle 缓存里的实际 jar 包核实存在，不是凭记忆假设的 API。
