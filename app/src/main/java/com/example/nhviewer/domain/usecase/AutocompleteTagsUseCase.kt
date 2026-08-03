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

        // 只对"当前正在输入的最后一段"做联想，前面已确认的关键词/标签不参与匹配
        val activeToken = query.trimEnd().substringAfterLast(' ')

        // 1. Instant local search
        val localResults = LocalTagIndex.searchByPrefix(activeToken, safeLang, 15).map {
            Tag(id = 0, name = it.name, type = it.type, count = 0, url = "")
        }
        emit(localResults)

        // 2. Fetch remote if last active token is plain ASCII prefix without filter markers
        val hasFilterSymbol = activeToken.any { it in ":><-" }
        val isAsciiOnly = activeToken.all { it.code < 128 }

        if (!hasFilterSymbol && isAsciiOnly && activeToken.isNotBlank()) {
            val remoteTags = repository.autocompleteTags(activeToken, type).getOrDefault(emptyList())

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
