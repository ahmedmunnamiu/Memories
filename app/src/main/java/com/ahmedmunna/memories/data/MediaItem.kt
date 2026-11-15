package com.ahmedmunna.memories.data

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val isVideo: Boolean,
    /** Capture time (falls back to date added), epoch millis. */
    val takenMs: Long,
    val addedMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val durationMs: Long,
    val bucketId: Long,
    val bucketName: String,
    val relativePath: String,
) {
    val isGif: Boolean get() = mimeType == "image/gif"

    val isRaw: Boolean
        get() = !isVideo && name.substringAfterLast('.', "").lowercase() in RAW_EXTENSIONS

    val isScreenshot: Boolean
        get() = relativePath.contains("screenshot", ignoreCase = true) ||
            bucketName.contains("screenshot", ignoreCase = true)

    val format: String
        get() = name.substringAfterLast('.', "").uppercase().ifEmpty { mimeType.substringAfter('/').uppercase() }

    companion object {
        private val RAW_EXTENSIONS = setOf("dng", "arw", "cr2", "cr3", "nef", "raf", "orf", "rw2", "pef", "srw")
    }
}

data class Album(
    val id: Long,
    val name: String,
    val items: List<MediaItem>,
) {
    val count: Int get() = items.size
    val cover: MediaItem get() = items.first()
    val newestMs: Long get() = items.maxOf { it.takenMs }
}
