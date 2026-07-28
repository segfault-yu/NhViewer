package com.example.nhviewer.presentation.feature.settings.about

/** 开源库致谢条目，库名与许可证均为专有名词，不参与多语言翻译 */
data class OpenSourceLibrary(
    val name: String,
    val version: String,
    val license: String
)

private const val LICENSE_APACHE_2 = "Apache-2.0"

/** 版本号需与 gradle/libs.versions.toml 保持同步 */
val OpenSourceLibraries: List<OpenSourceLibrary> = listOf(
    OpenSourceLibrary("Kotlin", "2.2.10", LICENSE_APACHE_2),
    OpenSourceLibrary("Jetpack Compose (Material 3)", "2025.12.00", LICENSE_APACHE_2),
    OpenSourceLibrary("Navigation Compose", "2.9.8", LICENSE_APACHE_2),
    OpenSourceLibrary("Hilt (Dagger)", "2.59.2", LICENSE_APACHE_2),
    OpenSourceLibrary("Hilt Navigation Compose", "1.2.0", LICENSE_APACHE_2),
    OpenSourceLibrary("Retrofit", "2.11.0", LICENSE_APACHE_2),
    OpenSourceLibrary("OkHttp", "4.12.0", LICENSE_APACHE_2),
    OpenSourceLibrary("Retrofit Kotlinx Serialization Converter", "1.0.0", LICENSE_APACHE_2),
    OpenSourceLibrary("Kotlinx Serialization", "1.7.3", LICENSE_APACHE_2),
    OpenSourceLibrary("Room", "2.7.0", LICENSE_APACHE_2),
    OpenSourceLibrary("Paging 3", "3.3.5", LICENSE_APACHE_2),
    OpenSourceLibrary("DataStore Preferences", "1.1.1", LICENSE_APACHE_2),
    OpenSourceLibrary("Security Crypto", "1.1.0-alpha06", LICENSE_APACHE_2),
    OpenSourceLibrary("Coil", "2.6.0", LICENSE_APACHE_2),
    OpenSourceLibrary("Telephoto", "0.19.0", LICENSE_APACHE_2)
)
