package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.SearchQuery
import javax.inject.Inject

class SearchQueryParser @Inject constructor() {

    fun parse(rawQuery: String): SearchQuery {
        if (rawQuery.isBlank()) {
            return SearchQuery(rawText = "")
        }

        // Match tokens: either quoted strings or non-space characters
        val tokenRegex = """"[^"]*"|\S+""".toRegex()
        val tokens = tokenRegex.findAll(rawQuery).map { it.value }.toList()

        val keywords = mutableListOf<String>()
        val includedTags = mutableListOf<String>()
        val excludedTags = mutableListOf<String>()
        var pagesCondition: String? = null
        var favoritesCondition: String? = null
        var uploadedCondition: String? = null

        val tagRegex = """^(-)?([a-zA-Z]+):(.+)$""".toRegex()
        val conditionRegex = """^(pages|favorites|uploaded):([><=]+)?(.+)$""".toRegex()

        for (token in tokens) {
            val condMatch = conditionRegex.matchEntire(token)
            if (condMatch != null) {
                val type = condMatch.groupValues[1]
                val op = condMatch.groupValues[2]
                val value = condMatch.groupValues[3]
                val conditionStr = "$op$value"

                when (type) {
                    "pages" -> pagesCondition = conditionStr
                    "favorites" -> favoritesCondition = conditionStr
                    "uploaded" -> uploadedCondition = conditionStr
                }
                continue
            }

            val tagMatch = tagRegex.matchEntire(token)
            if (tagMatch != null) {
                val isExcluded = tagMatch.groupValues[1] == "-"
                if (isExcluded) {
                    excludedTags.add(token)
                } else {
                    includedTags.add(token)
                }
                continue
            }

            // If it doesn't match specific patterns, treat as a keyword
            keywords.add(token)
        }

        return SearchQuery(
            rawText = rawQuery,
            keywords = keywords,
            includedTags = includedTags,
            excludedTags = excludedTags,
            pagesCondition = pagesCondition,
            favoritesCondition = favoritesCondition,
            uploadedCondition = uploadedCondition
        )
    }
}
