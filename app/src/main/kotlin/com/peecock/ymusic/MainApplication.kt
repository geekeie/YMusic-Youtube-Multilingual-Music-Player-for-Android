package com.peecock.ymusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.request.CachePolicy
import com.peecock.ymusic.enums.CoilDiskCacheMaxSize
import com.peecock.ymusic.utils.CaptureCrash
import com.peecock.ymusic.utils.FileLoggingTree
import com.peecock.ymusic.utils.coilDiskCacheMaxSizeKey
import com.peecock.ymusic.utils.getEnum
import com.peecock.ymusic.utils.logDebugEnabledKey
import com.peecock.ymusic.utils.preferences
import com.peecock.ymusic.R
import timber.log.Timber
import java.io.File

class MainApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer()

        /**** LOG *********/
        val logEnabled = preferences.getBoolean(logDebugEnabledKey, false)
        if (logEnabled) {
            val dir = filesDir.resolve("logs").also {
                if (it.exists()) return@also
                it.mkdir()
            }

            Thread.setDefaultUncaughtExceptionHandler(CaptureCrash(dir.absolutePath))

            Timber.plant(FileLoggingTree(File(dir, "RiMusic_log.txt")))
            Timber.d("Log enabled at ${dir.absolutePath}")
        } else {
            Timber.uprootAll()
            Timber.plant(Timber.DebugTree())
        }
        /**** LOG *********/
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .placeholder(R.drawable.loader)
            .error(R.drawable.app_icon)
            .fallback(R.drawable.app_icon)
            /*
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache(
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            )
             */
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache(
                DiskCache.Builder()
                    .directory(filesDir.resolve("coil"))
                    .maxSizeBytes(
                        preferences.getEnum(
                            coilDiskCacheMaxSizeKey,
                            CoilDiskCacheMaxSize.`128MB`
                        ).bytes
                    )
                    .build()
            )
            .build()
    }

}
