package com.ahmedmunna.memories.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SortOption(val label: String) {
    DATE("Date"), NAME("Name"), SIZE("Size"), TYPE("Type")
}

enum class MediaFilter(val label: String) {
    ALL("All"), PHOTOS("Photos"), VIDEOS("Videos"), SCREENSHOTS("Screenshots"), GIFS("GIFs"), RAW("RAW")
}

sealed interface GridEntry {
    data class Header(val key: String, val label: String) : GridEntry
    data class Media(val item: MediaItem) : GridEntry
}

object MediaOrganizer {

    fun filter(items: List<MediaItem>, filter: MediaFilter): List<MediaItem> = when (filter) {
        MediaFilter.ALL -> items
        MediaFilter.PHOTOS -> items.filter { !it.isVideo }
        MediaFilter.VIDEOS -> items.filter { it.isVideo }
        MediaFilter.SCREENSHOTS -> items.filter { it.isScreenshot }
        MediaFilter.GIFS -> items.filter { it.isGif }
        MediaFilter.RAW -> items.filter { it.isRaw }
    }

    fun sort(items: List<MediaItem>, sort: SortOption, descending: Boolean): List<MediaItem> {
        val cmp: Comparator<MediaItem> = when (sort) {
            SortOption.DATE -> compareBy { it.takenMs }
            SortOption.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortOption.SIZE -> compareBy { it.sizeBytes }
            SortOption.TYPE -> compareBy<MediaItem> { it.mimeType }.thenBy { it.takenMs }
        }
        return items.sortedWith(if (descending) cmp.reversed() else cmp)
    }

    /** Day headers only make sense for date sorting; other sorts produce a flat list. */
    fun group(items: List<MediaItem>, sort: SortOption): List<GridEntry> {
        if (sort != SortOption.DATE) return items.map { GridEntry.Media(it) }
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val out = ArrayList<GridEntry>(items.size + 32)
        var lastDay: LocalDate? = null
        for (item in items) {
            val day = Instant.ofEpochMilli(item.takenMs).atZone(zone).toLocalDate()
            if (day != lastDay) {
                out += GridEntry.Header("d${day.toEpochDay()}", dayLabel(day, today))
                lastDay = day
            }
            out += GridEntry.Media(item)
        }
        return out
    }

    private fun dayLabel(day: LocalDate, today: LocalDate): String = when {
        day == today -> "Today"
        day == today.minusDays(1) -> "Yesterday"
        day.year == today.year -> day.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
        else -> day.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
    }

    fun buildAlbums(all: List<MediaItem>): List<Album> =
        all.groupBy { it.bucketId }.map { (id, items) ->
            val sorted = items.sortedByDescending { it.takenMs }
            Album(id = id, name = sorted.first().bucketName, items = sorted)
        }
}
