package com.example.nhviewer.domain.model

data class SearchQuery(
    val rawText: String,
    val keywords: List<String> = emptyList(),
    val includedTags: List<String> = emptyList(),
    val excludedTags: List<String> = emptyList(),
    val pagesCondition: String? = null,
    val favoritesCondition: String? = null,
    val uploadedCondition: String? = null
) {
    fun toRawString(): String {
        val parts = mutableListOf<String>()
        parts.addAll(includedTags)
        parts.addAll(excludedTags)
        if (pagesCondition != null) parts.add("pages:$pagesCondition")
        if (favoritesCondition != null) parts.add("favorites:$favoritesCondition")
        if (uploadedCondition != null) parts.add("uploaded:$uploadedCondition")
        parts.addAll(keywords)
        return parts.joinToString(" ")
    }
}
