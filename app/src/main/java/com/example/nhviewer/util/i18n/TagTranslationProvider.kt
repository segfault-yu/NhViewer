package com.example.nhviewer.util.i18n

import android.content.Context
import com.example.nhviewer.domain.model.Tag
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

// 标签翻译提供者
object TagTranslationProvider {

    private val dictionaries = ConcurrentHashMap<String, Map<String, String>>()
    private var appContext: Context? = null

    // 初始化保存 Application Context
    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private fun getDictionary(lang: String): Map<String, String> {
        val safeLang = if (lang.isBlank()) "zh" else lang.lowercase()
        return dictionaries.getOrPut(safeLang) {
            loadDictionary(safeLang)
        }
    }

    private fun loadDictionary(lang: String): Map<String, String> {
        val context = appContext ?: return emptyMap()
        val mergedMap = HashMap<String, String>()

        val dirPath = "tags/$lang"
        try {
            val assetFiles = context.assets.list(dirPath)
            if (!assetFiles.isNullOrEmpty()) {
                for (file in assetFiles) {
                    if (file.endsWith(".json")) {
                        try {
                            context.assets.open("$dirPath/$file").use { inputStream ->
                                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                                    val jsonString = reader.readText()
                                    val map = Json.decodeFromString<Map<String, String>>(jsonString)
                                    mergedMap.putAll(map)
                                }
                            }
                        } catch (e: Exception) {
                            // 忽略单个文件解析异常
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略目录读取异常
        }

        // 若分类目录为空，读取旧版单 JSON 资产文件
        if (mergedMap.isEmpty()) {
            val fallbackFile = "tag_translations_$lang.json"
            try {
                context.assets.open(fallbackFile).use { inputStream ->
                    InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                        val jsonString = reader.readText()
                        val map = Json.decodeFromString<Map<String, String>>(jsonString)
                        mergedMap.putAll(map)
                    }
                }
            } catch (e: Exception) {
                // 忽略兜底文件读取异常
            }
        }

        return mergedMap
    }

    // 根据标签语言与显示模式获取格式化名称
    fun getFormattedName(
        rawName: String,
        targetLang: String = "zh",
        displayMode: String = "only_translation"
    ): String {
        if (displayMode == "only_original") {
            return rawName
        }

        val dict = getDictionary(targetLang)
        val lowerKey = rawName.lowercase().trim()
        val translation = dict[lowerKey]?.trim()

        if (translation.isNullOrEmpty()) {
            return rawName
        }

        return when (displayMode) {
            "bilingual" -> "$translation ($rawName)"
            else -> translation
        }
    }

    fun getFormattedName(
        tag: Tag,
        targetLang: String = "zh",
        displayMode: String = "only_translation"
    ): String {
        return getFormattedName(tag.name, targetLang, displayMode)
    }

    fun getTranslation(tag: Tag, targetLang: String = "zh"): String {
        return getFormattedName(tag.name, targetLang, "only_translation")
    }

    fun getTranslation(rawName: String, targetLang: String = "zh"): String {
        return getFormattedName(rawName, targetLang, "only_translation")
    }

    fun getDisplayWithOriginal(rawName: String, targetLang: String = "zh"): String {
        return getFormattedName(rawName, targetLang, "bilingual")
    }
}
