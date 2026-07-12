---
description: Jetpack Compose Material Design 3 (M3) 界面设计与美学实现规范
---

# Jetpack Compose Material Design 3 (M3) 界面设计与美学实现规范

为了彻底杜绝简陋、平铺、不符合现代 Android 美学的“毛坯房”式界面，后续所有涉及 Compose UI 的开发必须严格遵守本规范，构建具有高级质感、层次分明、动效优雅的 Material Design 3 原生风格界面。

---

## 1. 色彩与光影系统 (Color & Theming)

### 严格的语义化色彩
*   **严禁使用硬编码颜色值**（如 `Color.White`、`Color.Black` 或 `Color(0xFF...)`），除非是像阅读器黑屏这种特殊硬性场景。
*   必须使用 `MaterialTheme.colorScheme` 提供的语义化色彩：
    *   **主色调**：使用 `primary`，容器高亮使用 `primaryContainer` / `onPrimaryContainer`。
    *   **背景色层级**：外层脚手架使用 `background`；卡片、输入框、底栏等容器必须使用 `surface`、`surfaceContainer`、`surfaceVariant` 以拉开视觉深度。
    *   **文本与图标**：主标题用 `onSurface`，副文本/说明文字用 `onSurfaceVariant`，确保优秀的无障碍对比度。

---

## 2. 形状与卡片层级 (Shapes & Cards)

### 容器的圆角层级
*   避免统一使用固定的 `RoundedCornerShape(8.dp)`。应遵循 M3 的圆角系统：
    *   **小型组件**（按钮、Chip）：使用 `MaterialTheme.shapes.small` (8.dp) 或 `shapes.extraSmall` (4.dp)。
    *   **中型组件**（普通卡片、输入框）：使用 `MaterialTheme.shapes.medium` (12.dp) 或 `shapes.large` (16.dp)。
    *   **大型组件**（BottomSheet、对话框）：使用 `MaterialTheme.shapes.extraLarge` (28.dp)。

### 拒绝单一卡片样式
*   根据场景区分使用 M3 的三种卡片类型：
    1.  **`ElevatedCard`**：用于需要浮空感、吸引注意力的重要单项。
    2.  **`OutlinedCard`**：用于平铺列表中的元素，使用极细的 `surfaceVariant` 边框，质感极其内敛高级。
    3.  **`Card`**（填充型）：用于做区块划分的背景容器。

---

## 3. 排版与文本艺术 (Typography)

### 语义化排版分级
*   必须通过 `MaterialTheme.typography` 声明文本样式，以便完美适配系统字体缩放：
    *   **页面大标题**：`headlineMedium` / `titleLarge` (加粗 `FontWeight.Bold` 或 `ExtraBold`)。
    *   **卡片标题/条目**：`titleMedium` / `titleSmall` (中等粗细 `FontWeight.Medium` / `SemiBold`)。
    *   **正文/描述**：`bodyMedium` / `bodySmall`。
    *   **标签/徽章/辅助字**：`labelMedium` / `labelSmall`。

### 防御性排版
*   所有文本（特别是网络加载的画廊标题、标签名）**必须**做截断与溢出处理，避免超长文本撑爆布局：
    *   使用 `maxLines = 1`（或 2）以及 `overflow = TextOverflow.Ellipsis`。
    *   涉及数量的标签使用 `Spacer` 与 `weight(1f)` 配合，将元数据（如页数、时间）推至边缘，保证排版整齐。

---

## 4. MD3 标准高级组件替代方案 (Standard Components)

禁止手写简陋的原生 HTML 样式的行、列、按钮，必须使用以下 M3 高级组件：

*   **列表条目**：使用 **`ListItem`** 代替自定义的 Row 布局。它原生支持 `leadingContent` (如头像/图标)、`headlineContent` (主标题)、`supportingContent` (描述文本) 和 `trailingContent` (时间/操作)。
*   **流式标签**：使用 **`FilterChip`** 或 **`SuggestionChip`** 代替手写 Border + Text。
*   **导航栏**：使用 **`NavigationBar`** / **`NavigationDrawer`**，配合 `NavigationSuiteScaffold` 实现自适应的大屏/折叠屏导航。
*   **输入与搜索**：使用 **`SearchBar`** 或 **`DockedSearchBar`** 实现极具质感的遮罩拉伸式搜索体验。

---

## 5. 动效与触觉反馈 (Motion & Micro-interactions)

没有动效的 UI 犹如死水，必须合理嵌入以下轻量级动画：

*   **状态变化**：如卡片展开、列表高度变化，必须在 Modifier 上附加 `.animateContentSize()`。
*   **条件显示**：使用 `AnimatedVisibility` 代替简单的 `if (visible) { ... }`，并配置好 `fadeIn() + slideInVertically()` 进出场动效。
*   **图片加载**：使用 Coil 异步加载图片时，必须开启 `crossfade(true)` 淡入动画，拒绝生硬的图片闪现。
*   **点击波纹**：所有可点击区域必须绑定波纹效果（`.clickable`），如果需要全屏卡片点击，请确保点击事件挂载在卡片容器最外层，让整张卡片受波纹包裹。

---

## 6. 排布间距与栅格 (Grid & Spacing)

*   严格使用 **8dp 栅格系统** 控制间距：
    *   外边距（Content Padding）：统一使用 `16.dp` 或 `24.dp`。
    *   卡片内部间距：`12.dp` 或 `16.dp`。
    *   小元素间距（图标与文字之间）：`4.dp` 或 `8.dp`。
*   在 `Column` / `Row` 中排布多个子组件时，优先使用 `verticalArrangement = Arrangement.spacedBy(8.dp)` 替代在每个子组件中间插空 `Spacer`，使布局更加清晰、易维护。

---

## 7. 实例对照：从“丑陋”到“高级 MD3”

### ❌ 错误示范 (平庸粗糙的自定义行)
```kotlin
Row(
    modifier = Modifier.fillMaxWidth().padding(10.dp)
) {
    Image(painter = ..., contentDescription = null, modifier = Modifier.size(50.dp))
    Column(modifier = Modifier.padding(start = 10.dp)) {
        Text("画廊名称", fontSize = 18.sp, color = Color.Black)
        Text("20 页", fontSize = 14.sp, color = Color.Gray)
    }
}
```

###  正确示范 (纯正 MD3 质感)
```kotlin
OutlinedCard(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
            )
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$numPages 页",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
```
