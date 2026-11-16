package com.ahmedmunna.memories.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ahmedmunna.memories.data.GridEntry
import com.ahmedmunna.memories.data.MediaFilter
import com.ahmedmunna.memories.data.MediaItem
import com.ahmedmunna.memories.data.MediaOrganizer
import com.ahmedmunna.memories.data.SortOption
import com.ahmedmunna.memories.ui.GalleryUiState
import com.ahmedmunna.memories.ui.GalleryViewModel
import com.ahmedmunna.memories.ui.components.MediaGrid
import com.ahmedmunna.memories.util.MediaActions

/**
 * Timeline used by the Photos tab (all media) and by an opened album (that album's media).
 * Handles filtering, sorting, day grouping, multi-select, sharing and moving to trash.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaBrowser(
    title: String,
    scopeItems: List<MediaItem>,
    state: GalleryUiState,
    vm: GalleryViewModel,
    onBack: (() -> Unit)?,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val settings = state.settings

    val visible = remember(scopeItems, state.filter, settings.sort, settings.sortDescending) {
        MediaOrganizer.sort(MediaOrganizer.filter(scopeItems, state.filter), settings.sort, settings.sortDescending)
    }
    val entries = remember(visible, settings.sort) { MediaOrganizer.group(visible, settings.sort) }
    val selected = remember(visible, state.selection) { visible.filter { it.id in state.selection } }
    val selecting = state.selection.isNotEmpty()

    val trashLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        // The list refreshes itself through the MediaStore observer; just leave selection mode.
        vm.clearSelection()
    }
    fun trash(items: List<MediaItem>) {
        val sender = MediaActions.trashRequest(context, items.map { it.uri })
        if (sender == null) {
            Toast.makeText(context, "Moving to trash needs Android 11 or newer", Toast.LENGTH_LONG).show()
        } else {
            trashLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }
    }

    Column(modifier.fillMaxSize()) {
        if (selecting) {
            TopAppBar(
                title = { Text("${state.selection.size} selected") },
                navigationIcon = {
                    IconButton(onClick = vm::clearSelection) { Icon(Icons.Filled.Close, "Cancel selection") }
                },
                actions = {
                    IconButton(onClick = {
                        if (state.selection.size == visible.size) vm.clearSelection()
                        else vm.selectAll(visible.map { it.id })
                    }) { Icon(Icons.Filled.SelectAll, "Select all or none") }
                    IconButton(onClick = { MediaActions.share(context, selected) }) { Icon(Icons.Filled.Share, "Share") }
                    IconButton(onClick = { trash(selected) }) { Icon(Icons.Filled.Delete, "Move to trash") }
                },
            )
        } else {
            var sortMenu by remember { mutableStateOf(false) }
            var overflow by remember { mutableStateOf(false) }
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenu = true }) { Icon(Icons.AutoMirrored.Filled.Sort, "Sort") }
                        DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    trailingIcon = { if (settings.sort == option) Icon(Icons.Filled.Check, null) },
                                    onClick = { vm.setSort(option); sortMenu = false },
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(if (settings.sortDescending) "Newest / largest first ✓" else "Oldest / smallest first ✓") },
                                onClick = { vm.setSortDescending(!settings.sortDescending); sortMenu = false },
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { overflow = true }) { Icon(Icons.Filled.MoreVert, "More") }
                        DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
                            DropdownMenuItem(text = { Text("Settings") }, onClick = { overflow = false; onSettings() })
                            DropdownMenuItem(text = { Text("About Memories") }, onClick = { overflow = false; onAbout() })
                        }
                    }
                },
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                items(MediaFilter.entries) { f ->
                    FilterChip(selected = state.filter == f, onClick = { vm.setFilter(f) }, label = { Text(f.label) })
                }
            }
        }

        when {
            state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            visible.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
                Text(
                    if (scopeItems.isEmpty()) "No photos or videos found on this device."
                    else "Nothing matches the “${state.filter.label}” filter.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> MediaGrid(
                entries = entries,
                columns = settings.columns,
                selection = state.selection,
                onColumnsChange = vm::setColumns,
                onClick = { item ->
                    if (selecting) vm.toggleSelect(item.id)
                    else vm.openViewer(visible, visible.indexOfFirst { it.id == item.id }.coerceAtLeast(0))
                },
                onLongClick = { item -> vm.toggleSelect(item.id) },
                contentPadding = PaddingValues(bottom = 120.dp),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
