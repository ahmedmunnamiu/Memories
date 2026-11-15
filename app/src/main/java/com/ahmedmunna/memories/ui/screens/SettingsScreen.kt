package com.ahmedmunna.memories.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahmedmunna.memories.data.SortOption
import com.ahmedmunna.memories.data.ThemeMode
import com.ahmedmunna.memories.ui.GalleryUiState
import com.ahmedmunna.memories.ui.GalleryViewModel

/** Phase 1 settings: only options that are wired to real behaviour. Cloud/privacy settings arrive with their phases. */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(state: GalleryUiState, vm: GalleryViewModel, onBack: () -> Unit, onAbout: () -> Unit) {
    BackHandler(onBack = onBack)
    val s = state.settings
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionTitle("Appearance")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(selected = s.themeMode == mode, onClick = { vm.setTheme(mode) }, label = { Text(mode.label) })
                }
            }
            val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Dynamic colors", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (dynamicSupported) "Match your wallpaper colors" else "Requires Android 12 or newer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = s.dynamicColor && dynamicSupported, enabled = dynamicSupported, onCheckedChange = vm::setDynamicColor)
            }

            SectionTitle("Gallery")
            Text("Grid columns", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (2..5).forEach { c ->
                    FilterChip(selected = s.columns == c, onClick = { vm.setColumns(c) }, label = { Text("$c") })
                }
            }
            Text("Pinch on the photo grid to change this too.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Default sorting", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOption.entries.forEach { o ->
                    FilterChip(selected = s.sort == o, onClick = { vm.setSort(o) }, label = { Text(o.label) })
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Newest / largest first", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = s.sortDescending, onCheckedChange = vm::setSortDescending)
            }

            SectionTitle("Other")
            androidx.compose.material3.TextButton(onClick = onAbout) { Text("About Memories") }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp),
    )
}
