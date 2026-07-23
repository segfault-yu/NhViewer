package com.example.nhviewer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.example.nhviewer.data.local.SettingsManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class NhViewerApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun newImageLoader(): ImageLoader {
        val maxMb = runBlocking { settingsManager.maxImageCacheMb.first() }
        return ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(maxMb.toLong() * 1024L * 1024L)
                    .build()
            }
            .build()
    }
}
