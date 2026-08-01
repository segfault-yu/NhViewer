package com.example.nhviewer.util.i18n

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.compositionLocalOf
import com.example.nhviewer.domain.model.Tag
import java.util.Locale

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
 * 软件界面语言与 Context Locale 管理器
 */
object LanguageManager {

    /**
     * 仅覆盖 Resources、保留原 Context 引用链的包装类。
     * createConfigurationContext() 返回的是与 Activity 无关的裸 ContextImpl，
     * 若直接用它整体替换 LocalContext，会导致 hiltViewModel() 沿 ContextWrapper
     * 链向上找 Activity 时失败而崩溃，因此这里改为包裹原 Context 只换 Resources。
     */
    private class LocaleContextWrapper(
        base: Context,
        private val localizedResources: Resources
    ) : ContextWrapper(base) {
        override fun getResources(): Resources = localizedResources
    }

    /**
     * 根据 appLanguage 配置创建包装对应 Locale 语言环境的 Context
     * @param context 原始 Context
     * @param appLanguage 语言选项 ("system", "zh", "en")
     */
    fun createLocaleContext(context: Context, appLanguage: String): Context {
        val locale = when (appLanguage) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            else -> return context // 默认跟随系统语言
        }

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        val localizedResources = context.createConfigurationContext(config).resources
        return LocaleContextWrapper(context, localizedResources)
    }
}
