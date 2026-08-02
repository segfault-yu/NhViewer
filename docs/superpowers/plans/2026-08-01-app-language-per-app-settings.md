# 应用语言支持系统"每应用语言"设置 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让用户可以在系统"设置 → 应用 → 应用信息 → 语言"里直接切换本 App 的界面语言（Android 13+ 官方入口），同时保证 Android 10-12（API 29-32）上语言选择依然能跨 Activity 重建正确保留，并与 App 内设置页的语言选择双向一致。

**Architecture:** 用官方 AndroidX Per-app Language 机制（`android:localeConfig` 清单声明 + `AppCompatDelegate.setApplicationLocales()`）取代现有的手工 `LocalContext` 覆盖方案（`LanguageManager.createLocaleContext` / `ContextWrapper` hack）。`MainActivity` 改为继承 `AppCompatActivity`（该机制在 API 33 以下要求宿主 Activity 是 `AppCompatActivity` 才能正确持久化并在重建后保留语言选择），配套把 App 主题基类从纯系统 `android:Theme.Material.Light.NoActionBar` 换成 `Theme.AppCompat.DayNight.NoActionBar`。`SettingsManager.appLanguage`（DataStore）保留下来，但只作为"从旧版本升级时的一次性迁移种子"，此后语言的唯一事实来源是 `AppCompatDelegate`。

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, AndroidX AppCompat（新增依赖）、AndroidX Core（`LocaleListCompat`，已通过 `androidx-core-ktx` 传递引入）、JUnit4（现有单元测试基建，`isReturnDefaultValues = true`，无 Robolectric）。

## Global Constraints

- 不引入 `androidx.appcompat` 之外的任何新第三方依赖。
- 代码注释使用简体中文，简洁，不写显而易见的废话注释。
- 遵循项目现有的 DataStore/StateFlow/Hilt 既有模式，不做与本任务无关的重构。
- 每次改动后必须能通过 `./gradlew :app:compileDebugKotlin`（或更完整的 `assembleDebug`）编译。
- 主题基类与 Activity 基类的改动（Task 3、Task 4）属于全 App 级别的风险变更，必须在真机/模拟器上实测冷启动无崩溃、无异常闪烁后才可视为完成。

---

### Task 1: 引入 androidx.appcompat 依赖

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Produces: Gradle 依赖坐标 `libs.androidx.appcompat`，供后续任务在 `MainActivity.kt` 中 `import androidx.appcompat.app.AppCompatActivity` / `androidx.appcompat.app.AppCompatDelegate` 使用。

- [ ] **Step 1: 在 libs.versions.toml 中新增版本号与依赖坐标**

在 `[versions]` 块的"AndroidX 基础"分组下新增一行（紧跟 `activityCompose` 之后）：

```toml
activityCompose = "1.11.0"
appcompat = "1.7.0"
```

在 `[libraries]` 块的"AndroidX 基础"分组下新增一行（紧跟 `androidx-activity-compose` 之后）：

```toml
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
```

- [ ] **Step 2: 在 app/build.gradle.kts 的 dependencies 块中新增依赖**

紧跟 `implementation(libs.androidx.core.ktx)` 之后新增一行：

```kotlin
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
```

- [ ] **Step 3: 编译验证依赖解析成功**

Run: `./gradlew :app:compileDebugKotlin --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: 新增 androidx.appcompat 依赖，为官方每应用语言支持做准备"
```

---

### Task 2: 声明 locale-config 资源与清单属性

**Files:**
- Create: `app/src/main/res/xml/locales_config.xml`
- Modify: `app/src/main/AndroidManifest.xml:8-19`

**Interfaces:**
- Produces: 清单里的 `android:localeConfig` 属性，Android 13+ 系统据此在"应用信息 → 语言"里展示本 App 支持的语言列表（简体中文 / English）。

- [ ] **Step 1: 新建 locales_config.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<locale-config xmlns:android="http://schemas.android.com/apk/res/android">
    <locale android:name="zh-Hans" />
    <locale android:name="en" />
</locale-config>
```

- [ ] **Step 2: 在 AndroidManifest.xml 的 application 标签上新增 android:localeConfig**

```xml
    <application
        android:name=".NhViewerApplication"
        android:localeConfig="@xml/locales_config"

        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:enableOnBackInvokedCallback="true"
        android:theme="@style/Theme.NhViewer">
```

- [ ] **Step 3: 编译验证清单与资源合并成功**

Run: `./gradlew :app:processDebugResources --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/xml/locales_config.xml app/src/main/AndroidManifest.xml
git commit -m "feat: 声明 locale-config，接入 Android 13+ 系统每应用语言入口"
```

---

### Task 3: 主题基类切换为 AppCompat 系

**Files:**
- Modify: `app/src/main/res/values/themes.xml`

**Interfaces:**
- Produces: `Theme.NhViewer` 现在继承自 `Theme.AppCompat.DayNight.NoActionBar`，满足 Task 4 中 `AppCompatActivity` 对宿主主题的强制要求（否则 `super.onCreate()` 会抛 `InflateException: You need to use a Theme.AppCompat theme`）。

- [ ] **Step 1: 修改 themes.xml 的 parent**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.NhViewer" parent="Theme.AppCompat.DayNight.NoActionBar" />
</resources>
```

- [ ] **Step 2: 编译验证**

Run: `./gradlew :app:compileDebugKotlin --console=plain`
Expected: `BUILD SUCCESSFUL`（本步骤这时 `MainActivity` 还没改成 `AppCompatActivity`，纯 XML 主题改动本身不会让编译失败，真正验证要等 Task 4 装上模拟器实测）

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/themes.xml
git commit -m "feat: App 主题基类改为 Theme.AppCompat.DayNight，为 AppCompatActivity 铺垫"
```

---

### Task 4: LanguageManager 改为基于 AppCompatDelegate

**Files:**
- Modify: `app/src/main/java/com/example/nhviewer/util/i18n/LanguageManager.kt`
- Test: `app/src/test/java/com/example/nhviewer/util/i18n/LanguageManagerTest.kt`

**Interfaces:**
- Consumes: `androidx.appcompat.app.AppCompatDelegate`（Task 1 引入）
- Produces:
  - `LanguageManager.localesFor(appLanguage: String): LocaleListCompat` — 纯函数，"system"/"zh"/"en" → `LocaleListCompat`
  - `LanguageManager.applyAppLanguage(appLanguage: String): Unit` — 调用 `AppCompatDelegate.setApplicationLocales()` 真正生效
  - `LanguageManager.currentAppLanguage(): String` — 读取当前生效语言，映射回 "system"/"zh"/"en"，供 Task 6 的 `SettingsViewModel` 使用
  - 移除：`LanguageManager.createLocaleContext()`、私有类 `LocaleContextWrapper`（不再需要，官方机制不要求手工包装 Context）

- [ ] **Step 1: 编写失败的单元测试**

```kotlin
package com.example.nhviewer.util.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageManagerTest {

    @Test
    fun `zh 映射为简体中文 Locale`() {
        val locales = LanguageManager.localesFor("zh")

        assertEquals("zh-Hans", locales.toLanguageTags())
    }

    @Test
    fun `en 映射为英文 Locale`() {
        val locales = LanguageManager.localesFor("en")

        assertEquals("en", locales.toLanguageTags())
    }

    @Test
    fun `system 映射为空 LocaleList 跟随系统`() {
        val locales = LanguageManager.localesFor("system")

        assertTrue(locales.isEmpty)
    }

    @Test
    fun `未知取值也回退为跟随系统`() {
        val locales = LanguageManager.localesFor("unknown")

        assertTrue(locales.isEmpty)
    }
}
```

- [ ] **Step 2: 运行测试确认失败（此时 localesFor 还不存在）**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.nhviewer.util.i18n.LanguageManagerTest" --console=plain`
Expected: FAIL，报 `Unresolved reference: localesFor` 编译错误

- [ ] **Step 3: 重写 LanguageManager.kt**

```kotlin
package com.example.nhviewer.util.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import androidx.core.os.LocaleListCompat
import com.example.nhviewer.domain.model.Tag

/**
 * 标签显示模式 CompositionLocal 容器 (支持全局响应标签模式切换)
 * 可选值: "only_translation" (仅中文/翻译), "only_original" (仅英文原文), "bilingual" (双语对照)
 */
val LocalTagDisplayMode = compositionLocalOf { "only_translation" }

/**
 * 软件语言 CompositionLocal 容器
 */
val LocalTagLanguage = compositionLocalOf { "zh" }

/**
 * 已加入黑名单的标签 ID 集合 CompositionLocal 容器
 */
val LocalBlacklistedTagIds = compositionLocalOf<Set<Int>> { emptySet() }

/**
 * 添加标签至黑名单回调 CompositionLocal 容器
 */
val LocalAddToBlacklist = compositionLocalOf<(Tag) -> Unit> { {} }

/**
 * 软件界面语言管理器，基于官方 AndroidX Per-app Language 机制
 * (AppCompatDelegate.setApplicationLocales)，与系统"设置 -> 应用信息 -> 语言"共用同一套存储。
 */
object LanguageManager {

    /**
     * 把 appLanguage 配置("system"/"zh"/"en")转换为 LocaleListCompat
     */
    fun localesFor(appLanguage: String): LocaleListCompat = when (appLanguage) {
        "zh" -> LocaleListCompat.forLanguageTags("zh-Hans")
        "en" -> LocaleListCompat.forLanguageTags("en")
        else -> LocaleListCompat.getEmptyLocaleList() // 跟随系统语言
    }

    /**
     * 应用 appLanguage 配置为当前应用语言，会按需触发 Activity 重建
     */
    fun applyAppLanguage(appLanguage: String) {
        AppCompatDelegate.setApplicationLocales(localesFor(appLanguage))
    }

    /**
     * 读取当前生效的应用语言，映射回 "system"/"zh"/"en"
     */
    fun currentAppLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return "system"
        return when (locales.get(0)?.language) {
            "zh" -> "zh"
            "en" -> "en"
            else -> "system"
        }
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.nhviewer.util.i18n.LanguageManagerTest" --console=plain`
Expected: `BUILD SUCCESSFUL`，4 个测试全部 PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/nhviewer/util/i18n/LanguageManager.kt app/src/test/java/com/example/nhviewer/util/i18n/LanguageManagerTest.kt
git commit -m "feat: LanguageManager 改为基于 AppCompatDelegate 的官方每应用语言实现"
```

---

### Task 5: MainActivity 改为 AppCompatActivity，接入迁移逻辑，移除手工 LocalContext 覆盖

**Files:**
- Modify: `app/src/main/java/com/example/nhviewer/MainActivity.kt:1-120`

**Interfaces:**
- Consumes: `LanguageManager.applyAppLanguage`、`LanguageManager.currentAppLanguage`（Task 4）；`AppCompatActivity`、`AppCompatDelegate`（Task 1）
- Produces: `MainActivity` 不再手工覆盖 `LocalContext`；`NhViewerApp(userRepository)` 签名不变，不影响下游

- [ ] **Step 1: 替换 MainActivity.kt 第 1-120 行**

```kotlin
package com.example.nhviewer

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.nhviewer.domain.model.ReadingHistory
import com.example.nhviewer.presentation.feature.home.HomeViewModel
import com.example.nhviewer.presentation.navigation.AppDrawerContent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nhviewer.presentation.navigation.NhViewerNavGraph
import com.example.nhviewer.presentation.navigation.Route
import com.example.nhviewer.ui.theme.NhViewerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.nhviewer.data.local.SettingsManager
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.nhviewer.domain.model.AuthEvent
import com.example.nhviewer.domain.repository.UserRepository

import com.example.nhviewer.domain.repository.BlacklistRepository
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.i18n.LanguageManager
import com.example.nhviewer.util.i18n.LocalAddToBlacklist
import com.example.nhviewer.util.i18n.LocalBlacklistedTagIds
import com.example.nhviewer.util.i18n.LocalTagLanguage
import androidx.compose.runtime.CompositionLocalProvider
import com.example.nhviewer.util.i18n.LocalTagDisplayMode
import com.example.nhviewer.util.i18n.TagTranslationProvider

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var blacklistRepository: BlacklistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TagTranslationProvider.init(this)
        enableEdgeToEdge()

        // 迁移旧版本 DataStore 里保存的显式语言选择到官方 AppCompatDelegate API，
        // 仅在从未设置过(即刚从旧版本升级、或全新安装)时执行一次，
        // 避免覆盖系统设置或本次会话里已经生效的选择
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            lifecycleScope.launch {
                LanguageManager.applyAppLanguage(settingsManager.appLanguage.first())
            }
        }

        setContent {
            val themeMode by settingsManager.themeMode.collectAsState(initial = "system")
            val dynamicColor by settingsManager.dynamicColor.collectAsState(initial = true)
            val tagLanguage by settingsManager.tagLanguage.collectAsState(initial = "zh")
            val tagDisplayMode by settingsManager.tagDisplayMode.collectAsState(initial = "only_translation")
            val blacklistedTagIds by blacklistRepository.blacklistedTagIds.collectAsState()

            val scope = rememberCoroutineScope()

            val onAddToBlacklist: (Tag) -> Unit = remember {
                { tag ->
                    scope.launch {
                        blacklistRepository.addToBlacklist(tag)
                    }
                }
            }

            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(
                LocalTagLanguage provides tagLanguage,
                LocalTagDisplayMode provides tagDisplayMode,
                LocalBlacklistedTagIds provides blacklistedTagIds,
                LocalAddToBlacklist provides onAddToBlacklist
            ) {
                NhViewerTheme(
                    darkTheme = isDarkTheme,
                    dynamicColor = dynamicColor
                ) {
                    NhViewerApp(userRepository = userRepository)
                }
            }
        }
    }
}
```

`NhViewerApp` 及其后所有代码（原文件第 122 行起）保持不动，不需要修改（`androidx.compose.ui.platform.LocalContext.current` 在 `NhViewerApp` 里用的是全限定名，不依赖被删掉的 `import androidx.compose.ui.platform.LocalContext`）。

- [ ] **Step 2: 编译验证**

Run: `./gradlew :app:compileDebugKotlin --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/nhviewer/MainActivity.kt
git commit -m "feat: MainActivity 改为 AppCompatActivity，语言切换改走官方 AppCompatDelegate"
```

---

### Task 6: SettingsViewModel 对接新的语言读写方式

**Files:**
- Modify: `app/src/main/java/com/example/nhviewer/presentation/feature/settings/SettingsViewModel.kt:1-16,116-121,228-232`

**Interfaces:**
- Consumes: `LanguageManager.currentAppLanguage()`、`LanguageManager.applyAppLanguage()`（Task 4）
- Produces: `appLanguage: StateFlow<String>`、`setAppLanguage(language: String)` 对外签名保持不变，`SettingsScreen.kt` 无需任何改动

- [ ] **Step 1: 修改 import 块（第 1-11 行）**

```kotlin
package com.example.nhviewer.presentation.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhviewer.data.local.SettingsManager
import com.example.nhviewer.util.i18n.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
```

- [ ] **Step 2: 替换 appLanguage 声明（原第 116-121 行）**

```kotlin
    // 语言的事实来源是 AppCompatDelegate，不再从 DataStore 的 Flow 派生：
    // AppCompatDelegate.setApplicationLocales 触发的 Activity 重建会重新构造本 ViewModel，
    // 构造时重新读取一次即可反映系统设置里外部触发的语言变更
    private val _appLanguage = MutableStateFlow(LanguageManager.currentAppLanguage())
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()
```

- [ ] **Step 3: 替换 setAppLanguage 函数（原第 228-232 行）**

```kotlin
    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            settingsManager.setAppLanguage(language)
        }
        LanguageManager.applyAppLanguage(language)
        _appLanguage.value = language
    }
```

- [ ] **Step 4: 编译验证**

Run: `./gradlew :app:compileDebugKotlin --console=plain`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/nhviewer/presentation/feature/settings/SettingsViewModel.kt
git commit -m "feat: SettingsViewModel 语言状态改为读写 AppCompatDelegate"
```

---

### Task 7: 真机/模拟器手工验证（无法用单元测试替代）

**Files:** 无代码改动，仅验证。

以下场景涉及 Activity 重建、系统 Settings 交互、主题渲染，只能在真机或模拟器上验证，不在此写伪造的自动化测试：

- [ ] **Step 1: 全新安装冷启动**
  - 卸载旧版本后全新安装，冷启动不崩溃，日志里无 `InflateException` 或 Hilt `IllegalStateException`。
  - 深色系统模式下冷启动，确认没有明显的"先浅色闪一下再变深色"的异常闪烁（`Theme.AppCompat.DayNight` 理论上应比原来的强制浅色主题更平滑）。

- [ ] **Step 2: App 内切换语言（system / zh / en 三态互相切换）**
  - 进入设置页多次切换这三个选项，每次都应用生效、不崩溃，界面文案随之切换。
  - 切到具体语言后，退到后台再切回来，语言保持不变。

- [ ] **Step 3: 升级迁移路径**
  - 用 Task 6 之前的版本安装 App，在设置里选择"简体中文"或"English"（非 system）。
  - 不卸载，直接安装本次改造后的新版本（模拟应用内升级）。
  - 首次冷启动确认：语言仍然是升级前选的那个（验证 `MainActivity` 里的一次性迁移逻辑生效）。

- [ ] **Step 4: Android 13+ 设备/模拟器上验证系统入口**
  - 用 API 33+ 模拟器安装 App。
  - 进入系统"设置 → 应用 → NhViewer → 语言"，确认能看到"简体中文"/"English"选项并可以切换。
  - 在系统设置里切换语言后返回 App，确认 App 界面语言与 App 内设置页显示的"当前语言"都已同步为系统里选的那个（验证 `SettingsViewModel` 构造时重新读取 `LanguageManager.currentAppLanguage()` 生效）。

- [ ] **Step 5: API 29-32 模拟器验证（该 OS 版本没有系统入口，只能验证 App 内切换持久化）**
  - 用 API 29-32 模拟器安装 App，App 内切换语言后杀进程重启，确认语言保持（验证 `AppCompatActivity` 在这些版本上的向后兼容存储生效，而不是像纯 `ComponentActivity` 那样重启后打回系统默认）。

---

## Self-Review

- **Spec 覆盖**：新增依赖(Task1)、系统入口声明(Task2)、主题前置条件(Task3)、语言核心逻辑(Task4)、Activity 接入与迁移(Task5)、ViewModel 对接(Task6)、真机验证(Task7)，覆盖了目标里提到的"系统入口"“双向同步”“29-32 向后兼容”三个点。
- **占位符扫描**：全文没有 TODO / "适当处理" 之类的占位描述，每个 Step 都有完整代码或明确可执行的命令。
- **类型一致性**：`LanguageManager.localesFor` / `applyAppLanguage` / `currentAppLanguage` 三个函数名与签名在 Task4 定义后，在 Task5、Task6 里的引用一致；`SettingsViewModel.appLanguage` 对外仍是 `StateFlow<String>`，`SettingsScreen.kt` 的消费方式不需要改动。
