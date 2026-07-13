package com.example.nhviewer.domain.repository

import com.example.nhviewer.domain.model.Tag
import kotlinx.coroutines.flow.StateFlow

// 黑名单仓库接口
interface BlacklistRepository {
    // 观察已屏蔽标签 ID 集合的 StateFlow
    val blacklistedTagIds: StateFlow<Set<Int>>

    // 获取已屏蔽的标签列表
    suspend fun getBlacklist(): Result<List<Tag>>
    // 添加标签到黑名单（支持乐观更新及本地缓存）
    suspend fun addToBlacklist(tag: Tag): Result<Unit>
    // 从黑名单中移除指定 ID 标签
    suspend fun removeFromBlacklist(tagId: Int): Result<Unit>
    // 刷新黑名单数据
    suspend fun refreshBlacklist(): Result<Unit>
}
