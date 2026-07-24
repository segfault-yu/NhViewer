package com.example.nhviewer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.example.nhviewer.data.local.SettingsManager
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

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        CrashHandler.init()
        CoroutineScope(Dispatchers.IO).launch {
            val level = settingsManager.logLevel.first()
            AppLogger.setLogLevel(level)
        }
    }

    override fun newImageLoader(): ImageLoader {
        val maxMb = runBlocking { settingsManager.maxImageCacheMb.first() }
        return ImageLoader.Builder(this)
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor(com.example.nhviewer.data.remote.interceptor.UserAgentInterceptor())
                    .build()
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
