package com.example.nhviewer

import android.app.Application
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class NhViewerApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var galleryRepository: GalleryRepository

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
    }

    override fun newImageLoader(): ImageLoader {
        val maxMb = runBlocking { settingsManager.maxImageCacheMb.first() }
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
                    .maxSizeBytes(maxMb.toLong() * 1024L * 1024L)
                    .build()
            }
            .build()
    }
}
