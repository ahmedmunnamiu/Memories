package com.ahmedmunna.memories.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import com.ahmedmunna.memories.data.MediaItem
import java.util.Locale

object MediaActions {

    fun share(context: Context, items: List<MediaItem>) {
        if (items.isEmpty()) return
        val intent = if (items.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = items[0].mimeType
                putExtra(Intent.EXTRA_STREAM, items[0].uri)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(items.map { it.uri }))
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        launch(context, Intent.createChooser(intent, null))
    }

    fun openWith(context: Context, item: MediaItem) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(item.uri, item.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        launch(context, Intent.createChooser(intent, null))
    }

    fun setAsWallpaper(context: Context, item: MediaItem) {
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            setDataAndType(item.uri, item.mimeType)
            putExtra("mimeType", item.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        launch(context, Intent.createChooser(intent, "Set as wallpaper"))
    }

    fun openMap(context: Context, lat: Double, lon: Double) {
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
        launch(context, Intent(Intent.ACTION_VIEW, uri))
    }

    /**
     * Android 11+: system confirmation dialog that moves the items to the system Trash (restorable for 30 days).
     * Android 10 has no trash API; returns null and the caller tells the user.
     */
    fun trashRequest(context: Context, uris: List<Uri>): IntentSender? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            MediaStore.createTrashRequest(context.contentResolver, uris, true).intentSender
        } else {
            null
        }

    private fun launch(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No app available for this action", Toast.LENGTH_SHORT).show()
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var i = -1
    while (value >= 1024 && i < units.lastIndex) {
        value /= 1024
        i++
    }
    return String.format(Locale.getDefault(), "%.1f %s", value, units[i])
}

fun formatDuration(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    else String.format(Locale.getDefault(), "%d:%02d", m, s)
}
