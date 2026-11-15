package com.ahmedmunna.memories

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MemoriesApp : Application(), ImageLoaderFactory {

    /** One shared loader: bounded memory + disk cache, and video frame thumbnails. */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .components { add(VideoFrameDecoder.Factory()) }
        .crossfade(true)
        .memoryCache {
            MemoryCache.Builder(this).maxSizePercent(0.20).build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("thumbs"))
                .maxSizeBytes(256L * 1024 * 1024)
                .build()
        }
        .build()
}
