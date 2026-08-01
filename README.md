# NHViewer Manga Reader

**English** | [简体中文](README.zh-CN.md)

<img src="docs/images/icon.png" alt="NHViewer App Icon" width="120" />

A personal-use, open-source native Android app for reading and managing manga, built on the nhentai API v2.

---

## Project Overview

| Attribute | Choice |
|---|---|
| Language | Kotlin 2.2.10 |
| UI Framework | Jetpack Compose (Material 3 Expressive) |
| Architecture | Clean Architecture (data → domain → presentation) |
| Minimum SDK | API 29 (Android 10) |
| Target SDK | API 36 (Android 16) |
| Build Tool | Gradle (Kotlin DSL + Version Catalog) |
| License | MIT / Open Source |

---

## Tech Stack & Dependencies

| Role | Library / Technology | Version | Description |
|---|---|---|---|
| Core language | **Kotlin** | 2.2.10 | Statically typed, with coroutine support |
| Declarative UI | **Jetpack Compose BOM** | 2025.12.00 | Android's native declarative UI framework |
| Visual design | **Material 3 Expressive** | - | Latest components from `androidx.compose.material3` |
| Dependency injection | **Hilt** | 2.59.2 | Compile-time dependency injection |
| Async & data flow | **Kotlin Coroutines & Flow** | - | Reactive concurrency handling and state subscription |
| Networking | **Retrofit 2 & OkHttp 4** | 2.11.0 / 4.12.0 | RESTful API client with network interceptors |
| Data serialization | **Kotlinx Serialization** | 1.7.3 | Official high-performance JSON serializer/deserializer |
| Image loading | **Coil Compose** | 2.6.0 | Async image-loading library built for Compose |
| Image gesture zoom | **Telephoto** | 0.19.0 | High-performance gesture zoom and sub-sampling for large images |
| Local database | **Room** | 2.7.0 | SQLite abstraction layer used for history and caching |
| Key-value storage | **DataStore Preferences** | 1.1.1 | Reactive key-value storage replacing SharedPreferences |
| Encrypted storage | **Security Crypto** | 1.1.0-alpha06 | Encrypted token storage via EncryptedSharedPreferences |
| Navigation | **Navigation Compose** | 2.9.8 | Type-safe screen navigation, with Predictive Back support |
| Motion & shared elements | **Compose Animation** | - | `androidx.compose.animation`; drives transition motion and list-to-detail Hero shared-element transitions |
| Pagination | **Paging 3** | 3.3.5 | Streaming and pagination for large list data |

---

## Project Directory Structure

```
app/src/main/java/com/example/nhviewer/
├── MainActivity.kt                    // Global Activity entry point and CompositionLocal setup
├── NhViewerApp.kt                     // App root UI, Drawer, and global AuthEvent listener
│
├── data/                              // Data Layer
│   ├── local/                         // Local persistence
│   │   ├── dao/                       // Room DAO interfaces (ReadingHistoryDao, BlacklistTagDao, etc.)
│   │   ├── entity/                    // Room database entities (ReadingHistoryEntity, etc.)
│   │   ├── datastore/                 // DataStore settings wrapper (SettingsManager)
│   │   ├── TokenManager.kt            // Token storage & encryption manager
│   │   └── NhViewerDatabase.kt        // Room Database declaration
│   ├── paging/                        // Paging 3 data sources (FavoritesPagingSource, etc.)
│   ├── remote/                        // Remote API data sources
│   │   ├── dto/                       // API response DTOs
│   │   ├── interceptor/               // AuthInterceptor, UserAgentInterceptor, etc.
│   │   └── TokenRefreshAuthenticator.kt // 401 auto token-refresh authenticator
│   └── repository/                    // Local/network implementations of domain Repository interfaces
│
├── domain/                            // Domain Layer (pure Kotlin)
│   ├── model/                         // Core domain models (GalleryDetail, Tag, User, etc.)
│   ├── repository/                    // Repository interface abstractions
│   └── usecase/                       // Standalone business use cases (LoginUseCase, ReadingHistoryUseCase, etc.)
│
├── presentation/                      // Presentation Layer
│   ├── common/                        // Shared UI components across features
│   │   ├── TagChip.kt                 // Tag chip component
│   │   ├── GalleryCard.kt             // Gallery card component
│   │   ├── EmptyState.kt              // Empty-state placeholder
│   │   └── LoadingIndicator.kt        // Loading animation component
│   ├── feature/                       // UI screens & ViewModels organized by feature
│   │   ├── auth/                      // Login / register / password reset
│   │   ├── blacklist/                 // Blacklist management
│   │   ├── detail/                    // Gallery detail & tag list
│   │   ├── favorites/                 // Personal favorites
│   │   ├── history/                   // Reading history
│   │   ├── home/                      // Home page latest / popular galleries
│   │   ├── profile/                   // Profile & account settings
│   │   ├── reader/                    // Manga reader & thumbnail navigation bar
│   │   ├── search/                    // Search with smart autocomplete
│   │   ├── settings/                  // Settings (theme/language/API key/session/about)
│   │   ├── tagged/                    // Browse galleries by tag
│   │   └── tags/                      // Full tag list & category sorting
│   └── navigation/                    // Route definitions, NavGraph configuration
│       └── SharedTransitionScopes.kt  // CompositionLocal propagation for shared-element transition scopes
│
├── di/                                // Hilt DI modules (DatabaseModule, NetworkModule, etc.)
├── ui/theme/                          // Compose Material 3 theme (Color, Theme, Type, Shape)
│   └── Motion.kt                      // Single source of truth for motion tokens (NhMotion), includes Predictive Back transition composables
└── util/                              // Utilities (i18n, PoW solver, tag translator)
```

---

## Build & Environment Requirements

### Requirements

| Tool / Environment | Minimum | Recommended |
|---|---|---|
| **Android Studio** | Ladybug | 2024.2.1 or newer |
| **JDK version** | JDK 17 (hard requirement to run AGP 9.x) | JDK 21 |
| **Android SDK Min** | API 29 (Android 10) | - |
| **Android SDK Target / Compile** | API 36 (Android 16) | - |
| **Gradle** | Bundled Gradle Wrapper, automatically uses 9.4.1 — no separate install needed | - |

The Kotlin/Java bytecode target is pinned to **JVM 11** (kept consistent between `compileOptions` and the Kotlin `jvmTarget` in `app/build.gradle.kts`). This is separate from the JDK 17+ required to run Gradle itself — don't confuse the two.

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/rinchao0721/NhViewer.git
   cd NhViewer
   ```

2. **Configure `local.properties`**:
   Make sure `local.properties` in the project root points to the correct Android SDK path:
   ```properties
   sdk.dir=C\:\\Users\\username\\AppData\\Local\\Android\\Sdk
   ```

3. **Build the debug APK**:
   Use the project's bundled Gradle Wrapper to compile and run Kotlin syntax checks:
   ```bash
   # Run the Kotlin compilation check
   ./gradlew compileDebugKotlin

   # Build the debug APK
   ./gradlew assembleDebug
   ```
   Once the build succeeds, the APK is output to: `app/build/outputs/apk/debug/app-debug.apk`.

4. **Install and run**:
   With an Android device connected or an emulator running:
   ```bash
   ./gradlew installDebug
   ```

5. **Build a release version (optional)**:
   ```bash
   ./gradlew assembleRelease
   ```
   If `KEYSTORE_FILE` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` are all configured in `local.properties`, the output is signed automatically; otherwise an unsigned APK is produced (its filename gets a `-unsigned` suffix — for local debugging only, not for distribution). After the build, a copy is also placed at `app/build/outputs/release_apk/NHViewer-v<version>-release[-unsigned].apk`, without affecting the original output path.
