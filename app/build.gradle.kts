import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dagger.hilt)
}

android {
    namespace = "com.example.nhviewer"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.nhviewer"
        minSdk = 29
        targetSdk = 36
        versionCode = 7
        versionName = "0.3.5"

        testInstrumentationRunner = "com.example.nhviewer.HiltTestRunner"
    }

    signingConfigs {
        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localProps.load(FileInputStream(localPropsFile))
        }

        val keystoreFilePath = localProps.getProperty("KEYSTORE_FILE") ?: (project.findProperty("KEYSTORE_FILE") as? String)
        val keystorePassword = localProps.getProperty("KEYSTORE_PASSWORD") ?: (project.findProperty("KEYSTORE_PASSWORD") as? String)
        val keyAlias = localProps.getProperty("KEY_ALIAS") ?: (project.findProperty("KEY_ALIAS") as? String)
        val keyPassword = localProps.getProperty("KEY_PASSWORD") ?: (project.findProperty("KEY_PASSWORD") as? String)

        if (!keystoreFilePath.isNullOrEmpty() &&
            !keystorePassword.isNullOrEmpty() &&
            !keyAlias.isNullOrEmpty() &&
            !keyPassword.isNullOrEmpty()
        ) {
            val appDirFile = file(keystoreFilePath)
            val rootDirFile = rootProject.file(keystoreFilePath)
            val targetKeystore = if (appDirFile.exists()) appDirFile else if (rootDirFile.exists()) rootDirFile else null

            if (targetKeystore != null) {
                create("release") {
                    storeFile = targetKeystore
                    storePassword = keystorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                    enableV1Signing = true
                    enableV2Signing = true
                    enableV3Signing = false
                    enableV4Signing = false
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            // 让 android.util.Log 等静态桩返回默认值而非抛异常，无需引入 Robolectric
            isReturnDefaultValues = true
        }
    }
}

// 导出 Room schema 并纳入版本控制，后续升版本才有依据编写 Migration
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// object 是独立的单例类，不会像脚本顶层函数那样在 doLast 里隐式捕获整个脚本对象引用
// （脚本顶层 fun 是脚本类的成员方法，从 doLast 里调用会把脚本实例也序列化进去，破坏配置缓存）
object ReleaseArtifacts {
    /** 把 release APK 复制一份到独立目录，命名带项目名与版本号，原始产物保持不动。 */
    fun copyRenamedApk(
        logger: org.gradle.api.logging.Logger,
        apkDir: File,
        distDir: File,
        targetBaseName: String,
        isUnsigned: Boolean,
        missingArtifactMessage: String
    ) {
        val sourceFile = listOf("app-release.apk", "app-release-unsigned.apk")
            .map { File(apkDir, it) }
            .firstOrNull { it.exists() }
            ?: throw GradleException(missingArtifactMessage)

        if (isUnsigned) {
            logger.warn("未配置 release 签名，产出为未签名 APK，不可直接分发")
        }
        val targetName = "$targetBaseName${if (isUnsigned) "-unsigned" else ""}.apk"

        distDir.mkdirs()
        sourceFile.copyTo(File(distDir, targetName), overwrite = true)
    }
}

// 版本号、签名状态与目录 Provider 必须在 configureEach 内取成局部变量：
// 脚本顶层 val 会编译成脚本类字段，doLast 直接引用会持有脚本对象引用，破坏配置缓存
tasks.matching { it.name == "assembleRelease" }.configureEach {
    val appVersionName = android.defaultConfig.versionName ?: "0.0.0"
    val hasReleaseSigning = android.signingConfigs.findByName("release") != null
    val apkOutputDir = layout.buildDirectory.dir("outputs/apk/release")
    val apkDistDir = layout.buildDirectory.dir("outputs/release_apk")

    doLast {
        val apkDir = apkOutputDir.get().asFile
        val signedApkExists = File(apkDir, "app-release.apk").exists()

        ReleaseArtifacts.copyRenamedApk(
            logger = logger,
            apkDir = apkDir,
            distDir = apkDistDir.get().asFile,
            targetBaseName = "NHViewer-v$appVersionName-release",
            isUnsigned = !hasReleaseSigning || !signedApkExists,
            missingArtifactMessage = "assembleRelease 未产出 APK，请检查 ${apkDir.absolutePath}"
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Security Crypto
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Paging
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.telephoto.zoomable.image.coil)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.dagger.hilt.android.testing)
    kspAndroidTest(libs.dagger.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}