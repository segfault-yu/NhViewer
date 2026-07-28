package com.example.nhviewer.data.repository

import com.example.nhviewer.data.local.dao.BlacklistTagDao
import com.example.nhviewer.data.local.entity.BlacklistTagEntity
import com.example.nhviewer.data.local.entity.toDomain
import com.example.nhviewer.data.remote.BlacklistApi
import com.example.nhviewer.data.remote.dto.BlacklistRequest
import com.example.nhviewer.data.remote.dto.toDomain
import com.example.nhviewer.domain.model.Tag
import com.example.nhviewer.domain.repository.BlacklistRepository
import com.example.nhviewer.util.log.AppLogger
import com.example.nhviewer.util.runCatchingCancelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlacklistRepositoryImpl @Inject constructor(
    private val api: BlacklistApi,
    private val dao: BlacklistTagDao
) : BlacklistRepository {

    private val _blacklistedTagIds = MutableStateFlow<Set<Int>>(emptySet())
    override val blacklistedTagIds: StateFlow<Set<Int>> = _blacklistedTagIds.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            dao.getBlacklistFlow().collect { list ->
                _blacklistedTagIds.value = list.map { it.tagId }.toSet()
            }
        }
        scope.launch {
            refreshBlacklist()
        }
    }

    override suspend fun getBlacklist(): Result<List<Tag>> = runCatchingCancelable("Blacklist") {
        try {
            val response = api.getBlacklist()
            val remoteList = response.tags.map { it.toDomain() }

            if (remoteList.isNotEmpty()) {
                dao.clearAll()
                remoteList.forEach { tag ->
                    dao.insertTag(BlacklistTagEntity(tag.id, tag.name, tag.type))
                }
                remoteList
            } else {
                val localList = dao.getBlacklist().map { it.toDomain() }
                if (localList.isNotEmpty()) {
                    localList
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            AppLogger.w("Blacklist", "黑名单拉取失败，回退本地缓存", e)
            dao.getBlacklist().map { it.toDomain() }
        }
    }

    override suspend fun addToBlacklist(tag: Tag): Result<Unit> = runCatchingCancelable("Blacklist") {
        dao.insertTag(BlacklistTagEntity(tag.id, tag.name, tag.type))
        try {
            api.addToBlacklist(BlacklistRequest(tag.id))
            Unit
        } catch (e: IOException) {
            // Keep local
            AppLogger.w("Blacklist", "标签 ${tag.id} 加入黑名单上行失败，仅保留本地", e)
        }
    }

    override suspend fun removeFromBlacklist(tagId: Int): Result<Unit> = runCatchingCancelable("Blacklist") {
        dao.deleteTagById(tagId)
        try {
            val response = api.removeFromBlacklist(tagId)
            if (!response.isSuccessful) {
                throw Exception("API deletion failed")
            }
        } catch (e: IOException) {
            // Keep local
            AppLogger.w("Blacklist", "标签 $tagId 移出黑名单上行失败，仅保留本地", e)
        }
    }

    override suspend fun refreshBlacklist(): Result<Unit> = runCatchingCancelable("Blacklist") {
        getBlacklist()
        Unit
    }
}
