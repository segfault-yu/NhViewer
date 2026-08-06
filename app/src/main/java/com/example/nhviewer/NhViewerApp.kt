package com.example.nhviewer

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.example.nhviewer.data.local.SettingsManager
import com.example.nhviewer.data.remote.interceptor.CdnMirrorFallbackInterceptor
import com.example.nhviewer.domain.repository.GalleryRepository
import com.example.nhviewer.util.log.AppLogger
import com.example.nhviewer.util.log.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NhViewerApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var galleryRepository: GalleryRepository

    // 当前生效的磁盘缓存上限，用于判定设置变更后是否真的需要重建
    @Volatile
    private var currentImageCacheMb = SettingsManager.DEFAULT_MAX_IMAGE_CACHE_MB

    // 标记 Coil 是否已持有 ImageLoader：未创建过时可直接注入
    @Volatile
    private var imageLoaderCreated = false

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        CrashHandler.init()
        // 持续跟随设置变化，避免只读一次导致后续改动不生效
        CoroutineScope(Dispatchers.IO).launch {
            settingsManager.logLevel.collect { level ->
                AppLogger.setLogLevel(level)
            }
        }
        // 缓存上限同样持续跟随：改完即时重建，且读取放在 IO 线程避免阻塞主线程
        CoroutineScope(Dispatchers.IO).launch {
            settingsManager.maxImageCacheMb.collect { mb ->
                applyImageCacheLimit(mb)
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        imageLoaderCreated = true
        return buildImageLoader(currentImageCacheMb)
    }

    /**
     * 磁盘缓存上限变化时重建 ImageLoader。
     * 必须先 shutdown 旧实例释放目录占用：同一目录并存两个 DiskCache 会触发 DiskLruCache 文件锁冲突。
     */
    private fun applyImageCacheLimit(maxMb: Int) {
        if (maxMb < 0 || maxMb == currentImageCacheMb) return
        currentImageCacheMb = maxMb
        try {
            if (imageLoaderCreated) {
                Coil.imageLoader(this).shutdown()
            }
            Coil.setImageLoader(buildImageLoader(maxMb))
            imageLoaderCreated = true
        } catch (e: Exception) {
            AppLogger.w("ImageCache", "图片缓存上限调整失败 (maxMb=$maxMb)", e)
        }
    }

    private fun buildImageLoader(maxMb: Int): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                val builder = okhttp3.OkHttpClient.Builder()
                    // 镜像切换需在最外层：失败后携带新 host 重新进入后续拦截器，确保重试请求也带上 UA
                    .addInterceptor(CdnMirrorFallbackInterceptor(galleryRepository))
                    .addInterceptor(com.example.nhviewer.data.remote.interceptor.UserAgentInterceptor())
                if (BuildConfig.DEBUG) {
                    // 图片加载走独立的 OkHttpClient，未接入 API 日志拦截器，Debug 下单独补一份便于定位加载失败原因
                    builder.addInterceptor(
                        okhttp3.logging.HttpLoggingInterceptor().apply {
                            level = okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
                        }
                    )
                }
                builder.build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .apply {
                        if (maxMb == SettingsManager.UNLIMITED_IMAGE_CACHE_MB) {
                            // 不设固定上限，仅受可用空间约束
                            maxSizePercent(1.0)
                        } else {
                            maxSizeBytes(maxMb.toLong() * 1024L * 1024L)
                        }
                    }
                    .build()
            }
            .build()
    }
}
