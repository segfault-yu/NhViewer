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
