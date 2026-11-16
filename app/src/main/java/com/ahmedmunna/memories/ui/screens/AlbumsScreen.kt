package com.ahmedmunna.memories.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ahmedmunna.memories.data.Album

private enum class AlbumSort(val label: String) { RECENT("Most recent"), NAME("Name"), COUNT("Item count") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    albums: List<Album>,
    loading: Boolean,
    onOpen: (Long) -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sort by rememberSaveable { mutableStateOf(AlbumSort.RECENT) }
    var sortMenu by remember { mutableStateOf(false) }
    var overflow by remember { mutableStateOf(false) }

    val sorted = remember(albums, sort) {
        when (sort) {
            AlbumSort.RECENT -> albums.sortedByDescending { it.newestMs }
            AlbumSort.NAME -> albums.sortedBy { it.name.lowercase() }
            AlbumSort.COUNT -> albums.sortedByDescending { it.count }
        }
    }

    Column(modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Albums") },
            actions = {
                Box {
                    IconButton(onClick = { sortMenu = true }) { Icon(Icons.AutoMirrored.Filled.Sort, "Sort albums") }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        AlbumSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                trailingIcon = { if (sort == option) Icon(Icons.Filled.Check, null) },
                                onClick = { sort = option; sortMenu = false },
                            )
                        }
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
        if (sorted.isEmpty() && !loading) {
            Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
                Text("No albums yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(sorted, key = { it.id }) { album ->
                    Column(Modifier.fillMaxWidth().clickable { onOpen(album.id) }) {
                        AsyncImage(
                            model = album.cover.uri,
                            contentDescription = album.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(22.dp)),
                        )
                        Text(
                            album.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                        )
                        Text(
                            "${album.count} item${if (album.count == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
