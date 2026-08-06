package com.example.nhviewer.presentation.feature.settings.cache

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import com.example.nhviewer.data.local.GalleryMemoryCache
import com.example.nhviewer.util.log.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import javax.inject.Inject

/**
 * 缓存用量统计。
 * 内存快照存的是对象引用，无法折算字节数，只统计条目数。
 */
data class CacheUsage(
    val httpCacheBytes: Long = 0L,
    val imageDiskBytes: Long = 0L,
    val imageMemoryBytes: Long = 0L,
    val memorySnapshotEntries: Int = 0
) {
    /** 汇总仅计可度量字节的三项，不含内存快照 */
    val totalBytes: Long get() = httpCacheBytes + imageDiskBytes + imageMemoryBytes
}

@OptIn(ExperimentalCoilApi::class)
@HiltViewModel
class CacheViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpCache: Cache,
    private val memoryCache: GalleryMemoryCache
) : ViewModel() {

    private val _usage = MutableStateFlow(CacheUsage())
    val usage: StateFlow<CacheUsage> = _usage.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _clearing = MutableStateFlow(false)
    val clearing: StateFlow<Boolean> = _clearing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _usage.value = withContext(Dispatchers.IO) { collectUsage() }
            _loading.value = false
        }
    }

    /**
     * 清理全部缓存层。
     * 各层独立 try-catch：任一层失败不影响其余层继续清理。
     */
    fun clearAll(onFinished: (Boolean) -> Unit) {
        if (_clearing.value) return
        viewModelScope.launch {
            _clearing.value = true
            val success = withContext(Dispatchers.IO) {
                var allOk = true

                try {
                    httpCache.evictAll()
                } catch (e: Exception) {
                    allOk = false
                    AppLogger.w("Cache", "清理 API 缓存失败", e)
                }

                try {
                    val loader = imageLoader()
                    loader.diskCache?.clear()
                    loader.memoryCache?.clear()
                } catch (e: Exception) {
                    allOk = false
                    AppLogger.w("Cache", "清理图片缓存失败", e)
                }

                try {
                    memoryCache.clear()
                } catch (e: Exception) {
                    allOk = false
                    AppLogger.w("Cache", "清理内存快照失败", e)
                }

                allOk
            }
            _usage.value = withContext(Dispatchers.IO) { collectUsage() }
            _clearing.value = false
            onFinished(success)
        }
    }

    private fun collectUsage(): CacheUsage {
        val httpBytes = try {
            httpCache.size()
        } catch (e: Exception) {
            AppLogger.w("Cache", "读取 API 缓存用量失败", e)
            0L
        }

        var diskBytes = 0L
        var memoryBytes = 0L
        try {
            val loader = imageLoader()
            diskBytes = loader.diskCache?.size ?: 0L
            memoryBytes = loader.memoryCache?.size?.toLong() ?: 0L
        } catch (e: Exception) {
            AppLogger.w("Cache", "读取图片缓存用量失败", e)
        }

        return CacheUsage(
            httpCacheBytes = httpBytes,
            imageDiskBytes = diskBytes,
            imageMemoryBytes = memoryBytes,
            memorySnapshotEntries = memoryCache.entryCount()
        )
    }

    // 缓存上限变更会重建 ImageLoader，故每次现取而不持有引用
    private fun imageLoader(): ImageLoader = coil.Coil.imageLoader(context)
}
