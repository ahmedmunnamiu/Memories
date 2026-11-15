package com.ahmedmunna.memories.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Reads all device photos and videos from MediaStore and re-emits whenever the store changes. */
@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val filesUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

    fun observeMedia(): Flow<List<MediaItem>> = callbackFlow {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer)
        resolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, observer)
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }
        .conflate()
        .map { query(context.contentResolver) }
        .flowOn(Dispatchers.IO)

    private fun query(resolver: ContentResolver): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.RELATIVE_PATH,
        )
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
        )
        val result = ArrayList<MediaItem>()
        resolver.query(filesUri, projection, selection, args, null)?.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val iName = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val iMime = c.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val iType = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val iTaken = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            val iAdded = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val iSize = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val iW = c.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val iH = c.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val iDur = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
            val iBucket = c.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
            val iBucketName = c.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val iPath = c.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)

            while (c.moveToNext()) {
                val id = c.getLong(iId)
                val isVideo = c.getInt(iType) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val base = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val addedMs = c.getLong(iAdded) * 1000L
                val taken = c.getLong(iTaken)
                result += MediaItem(
                    id = id,
                    uri = ContentUris.withAppendedId(base, id),
                    name = c.getString(iName) ?: "",
                    mimeType = c.getString(iMime) ?: if (isVideo) "video/*" else "image/*",
                    isVideo = isVideo,
                    takenMs = if (taken > 0) taken else addedMs,
                    addedMs = addedMs,
                    sizeBytes = c.getLong(iSize),
                    width = c.getInt(iW),
                    height = c.getInt(iH),
                    durationMs = c.getLong(iDur),
                    bucketId = c.getLong(iBucket),
                    bucketName = c.getString(iBucketName) ?: "Other",
                    relativePath = c.getString(iPath) ?: "",
                )
            }
        }
        return result
    }
}
