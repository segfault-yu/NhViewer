package com.example.nhviewer.domain.usecase

import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.TagRepository
import com.example.nhviewer.util.i18n.TagTranslationProvider
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AutocompleteTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(query: String, type: String? = null, tagLanguage: String = "zh"): Result<List<Tag>> = coroutineScope {
        val isAsciiOnly = query.all { it.code < 128 }

        // 并行发起本地搜索和远程搜索
        val localDeferred = async { 
            runCatching { TagTranslationProvider.searchLocalTags(query, tagLanguage) }.getOrDefault(emptyList())
        }
        val remoteDeferred: kotlinx.coroutines.Deferred<List<Tag>>? = if (isAsciiOnly) {
            async { repository.autocompleteTags(query, type).getOrDefault(emptyList()) }
        } else null

        val localTags = localDeferred.await()
        val remoteTags = remoteDeferred?.await() ?: emptyList()

        val remoteTagsMap = remoteTags.associateBy { it.name }

        // 合并结果：本地匹配优先，接着是远程匹配。根据 name 去重。
        val mergedList = mutableListOf<Tag>()
        val seenNames = mutableSetOf<String>()

        for (tag in localTags) {
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

        Result.success(mergedList)
    }
}
