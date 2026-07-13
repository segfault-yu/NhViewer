package com.example.nhviewer.domain.repository

import com.example.nhviewer.domain.model.GalleryListItem
import kotlinx.coroutines.flow.Flow

// 收藏夹仓库接口
interface FavoriteRepository {
    // 观察收藏列表的 Flow 数据流
    val favoritesFlow: Flow<List<GalleryListItem>>

    // 获取收藏列表分页数据
    suspend fun getFavorites(page: Int): Result<List<GalleryListItem>>
    // 获取随机收藏的画廊
    suspend fun getRandomFavorite(): Result<GalleryListItem>
    // 检查是否已被收藏
    suspend fun checkIsFavorite(galleryId: Int): Result<Boolean>
    // 切换收藏状态（支持乐观更新及离线缓存）
    suspend fun toggleFavorite(gallery: GalleryListItem, isFavorite: Boolean): Result<Unit>
    // 同步离线的收藏/取消收藏改动队列到服务器
    suspend fun syncOfflineFavorites(): Result<Unit>
}
