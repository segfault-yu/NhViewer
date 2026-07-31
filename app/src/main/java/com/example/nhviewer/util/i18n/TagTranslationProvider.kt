package com.example.nhviewer.util.i18n

import android.content.Context
import android.util.JsonReader
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.util.log.AppLogger
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
                AppLogger.e("TagTranslation", "标签词典加载失败: $fileName", e)
            }
            map
        }
    }

    // For LocalTagIndex
    internal fun getDictionaryForIndex(lang: String, type: String): Map<String, String> {
        return getDictionary(lang, type)
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

        val allTypes = arrayOf("tag", "female", "male", "category", "language", "parody", "character", "artist", "group")

        if (type != null) {
            val dict = getDictionary(safeLang, type)
            translation = dict[lowerKey]

            // EhTagTranslation splits NHentai's "tag" type into "tag", "female", and "male".
            // Since NHentai API only returns "tag", we must check the other two if not found.
            // Furthermore, some tags like "non-h" might be classified as "category" in EhTagTranslation,
            // but returned as "tag" by NHentai. So we check ALL known types.
            if (translation == null) {
                for (fallbackType in allTypes) {
                    if (fallbackType == type.lowercase()) continue
                    translation = getDictionary(safeLang, fallbackType)[lowerKey]
                    if (translation != null) break
                }
            }
        } else {
            // 如果不知道 type，遍历所有的类型寻找
            for (fallbackType in allTypes) {
                translation = getDictionary(safeLang, fallbackType)[lowerKey]
                if (translation != null) break
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

    // 解析真正的标签类型 (将 tag 细分为 female/male)
    fun getTrueTagType(rawName: String, originalType: String = "tag", targetLang: String = "zh"): String {
        if (!originalType.equals("tag", ignoreCase = true)) return originalType.lowercase()

        val safeLang = if (targetLang.isBlank()) "zh" else targetLang.lowercase()
        val lowerKey = rawName.lowercase().trim()

        if (getDictionary(safeLang, "female").containsKey(lowerKey)) return "female"
        if (getDictionary(safeLang, "male").containsKey(lowerKey)) return "male"

        return "tag"
    }

    /** 把标签转成搜索语法里的一个词，如 `female:"束缚"`，供点击标签跳转搜索页时拼接查询串。 */
    fun toSearchQueryToken(tag: Tag, targetLang: String = "zh"): String {
        val trueType = getTrueTagType(tag.name, tag.type, targetLang)
        return "$trueType:\"${tag.name}\""
    }
}
