package com.ahmedmunna.memories.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ahmedmunna.memories.data.GridEntry
import com.ahmedmunna.memories.data.MediaItem
import com.ahmedmunna.memories.ui.components.MediaGrid
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/**
 * Search over the device library by file name, album name, year, month and type
 * (video, photo, screenshot, GIF, RAW, large). Every word you type must match something,
 * so "screenshot 2025" or "camera march" both narrow the results.
 *
 * Not included: searching by what is in a photo or by people's faces. That needs an
 * on-device recognition model, which this version does not have.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(state: GalleryUiState, vm: GalleryViewModel, modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    val recents = rememberSaveable(
        saver = listSaver<SnapshotStateList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() },
        ),
    ) { mutableStateListOf<String>() }
    val focus = LocalFocusManager.current

    val results = remember(state.all, query) {
        if (query.isBlank()) emptyList() else MediaSearch.search(state.all, query)
    }

    fun saveRecent(q: String) {
        val t = q.trim()
        if (t.isEmpty()) return
        recents.remove(t)
        recents.add(0, t)
        while (recents.size > 8) recents.removeAt(recents.lastIndex)
    }

    Column(modifier.fillMaxSize().statusBarsPadding()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text("Search by name, album, date or type") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Clear") }
                }
            },
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { saveRecent(query); focus.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )

        when {
            state.permissionGranted == false -> CenterText("Allow access to your photos (Photos tab) to search them.")

            query.isBlank() -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            ) {
                SectionLabel("Try")
                val year = LocalDate.now().year.toString()
                val albumNames = remember(state.albums) {
                    state.albums.sortedByDescending { it.count }.take(6).map { it.name }
                }
                val suggestions = listOf("Videos", "Screenshots", "GIFs", "Photos", "Large files" to "large", year)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    suggestions.forEach { s ->
                        val (label, term) = if (s is Pair<*, *>) (s.first as String) to (s.second as String) else (s as String) to (s as String)
                        AssistChip(onClick = { query = term }, label = { Text(label) })
                    }
                    albumNames.forEach { name ->
                        AssistChip(onClick = { query = name }, label = { Text(name) })
                    }
                }

                if (recents.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Recent searches",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { recents.clear() }) { Text("Clear") }
                    }
                    recents.forEach { r ->
                        Row(
                            Modifier.fillMaxWidth().clickable { query = r }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(r, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            }

            results.isEmpty() -> CenterText("No results for “${query.trim()}”.")

            else -> {
                Text(
                    "${results.size} result${if (results.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                val entries = remember(results) { results.map { GridEntry.Media(it) } }
                val open: (MediaItem) -> Unit = { item ->
                    saveRecent(query)
                    vm.openViewer(results, results.indexOfFirst { it.id == item.id }.coerceAtLeast(0))
                }
                MediaGrid(
                    entries = entries,
                    columns = state.settings.columns,
                    selection = emptySet(),
                    onColumnsChange = vm::setColumns,
                    onClick = open,
                    onLongClick = open,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
    )
}

@Composable
private fun CenterText(text: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private object MediaSearch {
    private val zone = ZoneId.systemDefault()
    private val monthNames = Month.values().map { it.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase() }

    fun search(items: List<MediaItem>, query: String): List<MediaItem> {
        val tokens = query.lowercase().split(' ', ',').filter { it.isNotBlank() }
        if (tokens.isEmpty()) return emptyList()
        return items
            .filter { item ->
                val date = Instant.ofEpochMilli(item.takenMs).atZone(zone)
                tokens.all { t -> matches(item, date.year, date.monthValue, t) }
            }
            .sortedByDescending { it.takenMs }
    }

    private fun matches(item: MediaItem, year: Int, month: Int, t: String): Boolean {
        if (item.name.contains(t, ignoreCase = true) || item.bucketName.contains(t, ignoreCase = true)) return true
        if (t.length == 4 && t.toIntOrNull() == year) return true
        if (t.length >= 3 && monthNames[month - 1].startsWith(t)) return true
        return when (t) {
            "video", "videos" -> item.isVideo
            "photo", "photos", "image", "images", "picture", "pictures" -> !item.isVideo
            "screenshot", "screenshots" -> item.isScreenshot
            "gif", "gifs" -> item.isGif
            "raw" -> item.isRaw
            "large", "big" -> item.sizeBytes >= 10L * 1024 * 1024
            else -> false
        }
    }
}
