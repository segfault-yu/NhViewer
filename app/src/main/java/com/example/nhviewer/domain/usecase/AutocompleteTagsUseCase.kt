package com.example.nhviewer.domain.usecase

import com.example.nhviewer.data.local.LocalTagIndex
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AutocompleteTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    operator fun invoke(query: String, type: String? = null, tagLanguage: String = "zh"): Flow<List<Tag>> = flow {
        val safeLang = if (tagLanguage.isBlank()) "zh" else tagLanguage.lowercase()

        // 1. Instant local search
        val localResults = LocalTagIndex.searchByPrefix(query, safeLang, 15).map {
            Tag(id = 0, name = it.name, type = it.type, count = 0, url = "")
        }
        emit(localResults)

        // 2. Fetch remote if ascii
        val isAsciiOnly = query.all { it.code < 128 }
        if (isAsciiOnly && query.isNotBlank()) {
            val remoteTags = repository.autocompleteTags(query, type).getOrDefault(emptyList())

            if (remoteTags.isNotEmpty()) {
                val remoteTagsMap = remoteTags.associateBy { it.name }
                val mergedList = mutableListOf<Tag>()
                val seenNames = mutableSetOf<String>()

                for (tag in localResults) {
                    if (seenNames.add(tag.name)) {
                        val remoteTag = remoteTagsMap[tag.name]
                        if (remoteTag != null) {
                            mergedList.add(tag.copy(count = remoteTag.count, url = remoteTag.url, id = remoteTag.id))
                        } else {
                            mergedList.add(tag)
                        }
                    }
                }
                for (tag in remoteTags) {
                    if (seenNames.add(tag.name)) {
                        mergedList.add(tag)
                    }
                }
                // 3. Emit merged results
                emit(mergedList)
            }
        }
    }
}
