package com.example.nhviewer.data.local

import com.example.nhviewer.domain.model.LocalTagResult
import com.example.nhviewer.util.i18n.TagTranslationProvider
import java.util.concurrent.ConcurrentHashMap

object LocalTagIndex {
    private val indexByLang = ConcurrentHashMap<String, List<LocalTagResult>>()
    private val allTypes = arrayOf("tag", "female", "male", "category", "language", "parody", "character", "artist", "group")

    private fun getOrBuildIndex(lang: String): List<LocalTagResult> {
        return indexByLang.getOrPut(lang) {
            val list = mutableListOf<LocalTagResult>()
            val seenKeys = mutableSetOf<String>()
            for (type in allTypes) {
                val dict = TagTranslationProvider.getDictionaryForIndex(lang, type)
                for ((key, value) in dict) {
                    if (seenKeys.add(key)) {
                        val resolvedType = if (type == "female" || type == "male") "tag" else type
                        list.add(LocalTagResult(name = key, type = resolvedType, translatedName = value))
                    }
                }
            }
            list
        }
    }

    fun searchByPrefix(query: String, lang: String, limit: Int = 15): List<LocalTagResult> {
        if (query.isBlank()) return emptyList()
        val lowerQuery = query.lowercase().trim()
        val index = getOrBuildIndex(lang)

        val matched = mutableListOf<LocalTagResult>()
        for (item in index) {
            if (item.name.contains(lowerQuery, ignoreCase = true) || item.translatedName.contains(lowerQuery, ignoreCase = true)) {
                matched.add(item)
            }
        }

        matched.sortWith(Comparator { a, b ->
            val aKey = a.name.lowercase()
            val aValue = a.translatedName.lowercase()
            val bKey = b.name.lowercase()
            val bValue = b.translatedName.lowercase()

            val aExact = aKey == lowerQuery || aValue == lowerQuery
            val bExact = bKey == lowerQuery || bValue == lowerQuery
            if (aExact && !bExact) return@Comparator -1
            if (!aExact && bExact) return@Comparator 1

            val aPrefix = aKey.startsWith(lowerQuery) || aValue.startsWith(lowerQuery)
            val bPrefix = bKey.startsWith(lowerQuery) || bValue.startsWith(lowerQuery)
            if (aPrefix && !bPrefix) return@Comparator -1
            if (!aPrefix && bPrefix) return@Comparator 1

            val aMinLen = minOf(aKey.length, aValue.length)
            val bMinLen = minOf(bKey.length, bValue.length)
            aMinLen.compareTo(bMinLen)
        })

        return matched.take(limit)
    }
}
