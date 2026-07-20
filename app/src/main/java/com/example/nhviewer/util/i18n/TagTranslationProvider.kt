package com.example.nhviewer.util.i18n

import android.content.Context
import android.util.JsonReader
import android.util.Log
import com.example.nhviewer.domain.model.Tag
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

// 标签翻译提供者 (高性能流式按需加载版)
object TagTranslationProvider {

    // 格式: ${lang}_${type} -> Map<String, String>
    private val categoryDictionaries = ConcurrentHashMap<String, ConcurrentHashMap<String, String>>()
    private var appContext: Context? = null

    // 初始化保存 Application Context
    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private fun getDictionary(lang: String, type: String): Map<String, String> {
        val context = appContext ?: return emptyMap()
        val key = "${lang}_$type"

        return categoryDictionaries.getOrPut(key) {
            val map = ConcurrentHashMap<String, String>()
            val fileName = "tags/$lang/$type.json"
            try {
                val assetManager = context.assets
                // 检查文件是否存在
                val exists = try {
                    assetManager.open(fileName).use { }
                    true
                } catch (e: Exception) {
                    false
                }

                if (exists) {
                    // 使用原生的流式解析 JsonReader，内存消耗极低，速度极快
                    assetManager.open(fileName).use { inputStream ->
                        InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                            val jsonReader = JsonReader(reader)
                            jsonReader.beginObject()
                            while (jsonReader.hasNext()) {
                                val name = jsonReader.nextName()
                                val value = jsonReader.nextString()
                                map[name] = value
                            }
                            jsonReader.endObject()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TagTranslation", "Failed to load dictionary: $fileName", e)
            }
            map
        }
    }

    // 根据标签语言与显示模式获取格式化名称（指定 type 会触发按需加载）
    fun getFormattedName(
        rawName: String,
        type: String?,
        targetLang: String = "zh",
        displayMode: String = "only_translation"
    ): String {
        if (displayMode == "only_original") {
            return rawName
        }

        val safeLang = if (targetLang.isBlank()) "zh" else targetLang.lowercase()
        val lowerKey = rawName.lowercase().trim()

        var translation: String? = null

        if (type != null) {
            val dict = getDictionary(safeLang, type)
            translation = dict[lowerKey]
        } else {
            // 如果不知道 type，只能去已加载的字典里碰运气
            for (dict in categoryDictionaries.values) {
                val t = dict[lowerKey]
                if (t != null) {
                    translation = t
                    break
                }
            }
        }

        translation = translation?.trim()

        if (translation.isNullOrEmpty()) {
            return rawName
        }

        return when (displayMode) {
            "bilingual" -> "$translation ($rawName)"
            else -> translation
        }
    }

    // 兼容之前的旧接口（不知道 type）
    fun getFormattedName(
        rawName: String,
        targetLang: String = "zh",
        displayMode: String = "only_translation"
    ): String {
        return getFormattedName(rawName, null, targetLang, displayMode)
    }

    // 新增：知道 tag type 的入口
    fun getFormattedName(
        tag: Tag,
        targetLang: String = "zh",
        displayMode: String = "only_translation"
    ): String {
        return getFormattedName(tag.name, tag.type, targetLang, displayMode)
    }

    fun getTranslation(tag: Tag, targetLang: String = "zh"): String {
        return getFormattedName(tag, targetLang, "only_translation")
    }

    fun getTranslation(rawName: String, targetLang: String = "zh"): String {
        return getFormattedName(rawName, null, targetLang, "only_translation")
    }

    fun getDisplayWithOriginal(rawName: String, targetLang: String = "zh"): String {
        return getFormattedName(rawName, null, targetLang, "bilingual")
    }
}
