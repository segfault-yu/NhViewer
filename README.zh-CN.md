# NHViewer 漫画阅读器

[English](README.md) | **简体中文**

<img src="docs/images/icon.png" alt="NHViewer 应用图标" width="120" />

基于 nhentai API v2 构建的个人自用、开源 Android 原生漫画阅读与管理应用。

---

## 项目概览

| 属性 | 选型 |
|---|---|
| 编程语言 | Kotlin 2.2.10 |
| UI 框架 | Jetpack Compose (Material 3 Expressive) |
| 架构设计 | Clean Architecture (data → domain → presentation) |
| 最低 SDK 支持 | API 29 (Android 10) |
| 目标 SDK 版本 | API 36 (Android 16) |
| 核心构建工具 | Gradle (Kotlin DSL + Version Catalog) |
| 软件授权 | MIT / 开源 |

---

## 技术栈与依赖

| 职责分工 | 开源库 / 技术选型 | 版本号 | 说明 |
|---|---|---|---|
| 核心语言 | **Kotlin** | 2.2.10 | 强类型、协程支持 |
| 声明式 UI | **Jetpack Compose BOM** | 2025.12.00 | Android 原生响应式 UI 框架 |
| 视觉设计 | **Material 3 Expressive** | - | `androidx.compose.material3` 最新组件 |
| 依赖注入 | **Hilt** | 2.59.2 | 编译期依赖注入 |
| 异步与数据流 | **Kotlin Coroutines & Flow** | - | 响应式并发处理与状态订阅 |
| 网络请求 | **Retrofit 2 & OkHttp 4** | 2.11.0 / 4.12.0 | RESTful API 客户端与网络拦截器 |
| 数据序列化 | **Kotlinx Serialization** | 1.7.3 | 官方高效 JSON 编解码器 |
| 图片加载 | **Coil Compose** | 2.6.0 | Compose 专属异步图片加载库 |
| 图片手势缩放 | **Telephoto** | 0.19.0 | 高性能大图手势缩放与采样组件 |
| 本地数据库 | **Room** | 2.7.0 | SQLite 抽象层，用于历史与缓存 |
| 键值存储 | **DataStore Preferences** | 1.1.1 | 替代 SharedPreferences 的响应式存储 |
| 安全加密存储 | **Security Crypto** | 1.1.0-alpha06 | EncryptedSharedPreferences 加密 Token 存储 |
| 导航路由 | **Navigation Compose** | 2.9.8 | 类型安全（Type-safe）的页面导航，含预见式返回（Predictive Back）支持 |
| 动效与共享元素 | **Compose Animation** | - | `androidx.compose.animation`，驱动转场动效与列表→详情 Hero 共享元素转场 |
| 数据分页 | **Paging 3** | 3.3.5 | 列表大数据流式加载与分页管理 |

---

## 项目目录结构

```
app/src/main/java/com/example/nhviewer/
├── MainActivity.kt                    // 全局 Activity 入口与 CompositionLocal 配置
├── NhViewerApp.kt                     // 应用根 UI、Drawer 与全局 AuthEvent 事件监听
│
├── data/                              // 数据层 (Data Layer)
│   ├── local/                         // 本地持久化存储
│   │   ├── dao/                       // Room DAO 接口 (ReadingHistoryDao, BlacklistTagDao 等)
│   │   ├── entity/                    // Room 数据库实体 (ReadingHistoryEntity 等)
│   │   ├── datastore/                 // DataStore 设置项封装 (SettingsManager)
│   │   ├── TokenManager.kt            // 存储与加密 Token 管理类
│   │   └── NhViewerDatabase.kt        // Room Database 数据库声明
│   ├── paging/                        // Paging 3 分页数据源 (FavoritesPagingSource 等)
│   ├── remote/                        // 远程 Api 数据源
│   │   ├── dto/                       // API 响应数据传输对象 (DTO)
│   │   ├── interceptor/               // AuthInterceptor, UserAgentInterceptor 等
│   │   └── TokenRefreshAuthenticator.kt // 401 自动刷新 Token 校验器
│   └── repository/                    // 领域层 Repository 接口的本地/网络实现
│
├── domain/                            // 领域层 (Domain Layer - 纯 Kotlin)
│   ├── model/                         // 核心领域数据模型 (GalleryDetail, Tag, User 等)
│   ├── repository/                    // 数据仓库接口抽象定义
│   └── usecase/                       // 独立业务用例 (LoginUseCase, ReadingHistoryUseCase 等)
│
├── presentation/                      // 视图层 (Presentation Layer)
│   ├── common/                        // 跨功能模块通用 UI 组件
│   │   ├── TagChip.kt                 // 标签气泡组件
│   │   ├── GalleryCard.kt             // 画廊卡片组件
│   │   ├── EmptyState.kt              // 空状态占位页
│   │   └── LoadingIndicator.kt        // 加载动画组件
│   ├── feature/                       // 按业务功能划分的 UI 界面与 ViewModel
│   │   ├── auth/                      // 登录 / 注册 / 密码重置
│   │   ├── blacklist/                 // 黑名单管理
│   │   ├── detail/                    // 画廊详情与标签列表
│   │   ├── favorites/                 // 个人收藏夹
│   │   ├── history/                   // 阅读历史记录
│   │   ├── home/                      // 首页最新 / 热门画廊
│   │   ├── profile/                   // 个人中心与资料修改
│   │   ├── reader/                    // 漫画阅读器与缩略图导航栏
│   │   ├── search/                    // 搜索与智能自动补全
│   │   ├── settings/                  // 设置 (主题/语言/API Key/会话/关于)
│   │   ├── tagged/                    // 按标签浏览画廊
│   │   └── tags/                      // 标签大全与分类排序
│   └── navigation/                    // 路由路径 (Route)、NavGraph 导航图配置
│       └── SharedTransitionScopes.kt  // 共享元素转场 Scope 的 CompositionLocal 下发
│
├── di/                                // Hilt 依赖注入模块 (DatabaseModule, NetworkModule 等)
├── ui/theme/                          // Compose Material 3 主题样式 (Color, Theme, Type, Shape)
│   └── Motion.kt                      // 动效令牌唯一真源 (NhMotion)，含预见式返回转场组合
└── util/                              // 工具类 (i18n 多语言、PoW 解算工具、标签翻译器)
```

---

## 构建与环境要求

### 环境要求

| 工具/环境 | 最低要求 | 推荐配置 |
|---|---|---|
| **Android Studio** | Ladybug | 2024.2.1 或更新版本 |
| **JDK 版本** | JDK 17（运行 AGP 9.x 的硬性要求） | JDK 21 |
| **Android SDK Min** | API 29 (Android 10) | - |
| **Android SDK Target / Compile** | API 36 (Android 16) | - |
| **Gradle** | 项目自带 Gradle Wrapper，自动使用 9.4.1，无需单独安装 | - |

Kotlin/Java 字节码目标版本固定为 **JVM 11**（`app/build.gradle.kts` 中 `compileOptions` 与 Kotlin `jvmTarget` 已保持一致），与运行 Gradle 本身所需的 JDK 17+ 是两回事，不要混淆。

### 编译与构建步骤

1. **克隆项目到本地**：
   ```bash
   git clone https://github.com/rinchao0721/NhViewer.git
   cd NhViewer
   ```

2. **配置 local.properties**：
   在项目根目录下确保 `local.properties` 指向正确的 Android SDK 路径：
   ```properties
   sdk.dir=C\:\\Users\\username\\AppData\\Local\\Android\\Sdk
   ```

3. **编译调试版本 (Debug APK)**：
   使用项目内置的 Gradle Wrapper 执行编译与 Kotlin 语法检查：
   ```bash
   # 执行 Kotlin 源代码编译检查
   ./gradlew compileDebugKotlin

   # 构建 Debug 安装包
   ./gradlew assembleDebug
   ```
   编译成功后，APK 输出路径为：`app/build/outputs/apk/debug/app-debug.apk`。

4. **安装并运行**：
   连接 Android 设备或启动模拟器后执行：
   ```bash
   ./gradlew installDebug
   ```

5. **构建 Release 版本（可选）**：
   ```bash
   ./gradlew assembleRelease
   ```
   若在 `local.properties` 中配置了 `KEYSTORE_FILE`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` 四项签名信息，产物会自动签名；未配置则产出未签名 APK（文件名会带 `-unsigned` 后缀，仅供本地调试，不可直接分发）。构建完成后会额外复制一份到 `app/build/outputs/release_apk/NHViewer-v<版本号>-release[-unsigned].apk`，原始产物路径不受影响。
