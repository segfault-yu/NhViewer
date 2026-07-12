# 项目定制规则

以下是针对本项目开发过程中的特殊约定和准则，后续所有 Agent 均须严格遵守。

## Material Design 3 (M3) Jetpack Compose 界面设计与美学实现规范

为了彻底杜绝简陋、平铺、不符合现代 Android 美学的“毛坯房”式界面，所有涉及 Compose UI 的开发必须严格遵守以下规范，构建具有高级质感、层次分明、动效优雅的 Material Design 3 原生风格界面：

1. **色彩与光影系统 (Color & Theming)**
   * **严禁硬编码颜色值**。必须使用 `MaterialTheme.colorScheme` 的语义化色彩。
   * 背景色要拉开层级。外层使用 `background`，卡片或容器使用 `surfaceContainer`、`surfaceVariant` 等，文本用 `onSurface` 或 `onSurfaceVariant`。

2. **形状与卡片层级 (Shapes & Cards)**
   * 根据 M3 规范选用圆角：小组件用 `shapes.small` (8.dp)，中型组件（卡片）用 `shapes.medium` (12.dp) 或 `shapes.large` (16.dp)。
   * 卡片区分场景使用 `OutlinedCard`（内敛感）、`ElevatedCard`（浮空感）或填充 `Card`（做容器分区块）。

3. **排版与文本艺术 (Typography)**
   * 字体大小均应通过 `MaterialTheme.typography` 声明（如 `titleLarge`, `bodyMedium`, `labelSmall`）。
   * 文本**必须**进行防御性排版，配置 `maxLines` 和 `overflow = TextOverflow.Ellipsis`，防止溢出。

4. **高级组件替代方案 (M3 Standard Components)**
   * 禁止手写粗糙的自定义行。复杂列表项优先使用 **`ListItem`**，它自带优秀的排版结构。
   * 用 **`FilterChip`** 或 **`SuggestionChip`** 替代手写的 Tag 边框。
   * 输入或搜索优先使用 **`SearchBar`** 或 **`DockedSearchBar`**。

5. **动效与触觉反馈 (Motion & Micro-interactions)**
   * 高度或尺寸变化时使用 `.animateContentSize()` 顺滑过渡。
   * 图片加载必须启用 Coil 的 `crossfade(true)` 淡入。
   * 条件渲染使用 `AnimatedVisibility` 代替粗暴的 `if` 显隐。

6. **排布间距与布局 (Spacing & Layout)**
   * 严格遵守 8dp 栅格系统 (`4.dp`, `8.dp`, `12.dp`, `16.dp`, `24.dp`)。
   * 在 Row/Column 中，优先使用 `Arrangement.spacedBy()` 来控制间距，而非随意插入 `Spacer`。
