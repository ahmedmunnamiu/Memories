package com.ahmedmunna.memories.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedmunna.memories.ui.screens.AboutScreen
import com.ahmedmunna.memories.ui.screens.AlbumsScreen
import com.ahmedmunna.memories.ui.screens.MediaBrowser
import com.ahmedmunna.memories.ui.screens.PermissionScreen
import com.ahmedmunna.memories.ui.screens.SettingsScreen
import com.ahmedmunna.memories.ui.screens.ViewerScreen

private enum class Tab(val label: String, val icon: ImageVector) {
    PHOTOS("Photos", Icons.Filled.PhotoLibrary),
    ALBUMS("Albums", Icons.Filled.Collections),
    SEARCH("Search", Icons.Filled.Search),
}

private enum class Overlay { SETTINGS, ABOUT }

@Composable
fun MainScreen(vm: GalleryViewModel, onRequestPermission: () -> Unit) {
    val state by vm.ui.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.PHOTOS) }
    var overlay by rememberSaveable { mutableStateOf<Overlay?>(null) }

    val selecting = state.selection.isNotEmpty()

    // Back priority: viewer > overlay > selection > open album.
    BackHandler(enabled = state.viewer == null && overlay != null) { overlay = null }
    BackHandler(enabled = state.viewer == null && overlay == null && selecting) { vm.clearSelection() }
    BackHandler(enabled = state.viewer == null && overlay == null && !selecting && state.openAlbumId != null) {
        vm.openAlbum(null)
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                AnimatedVisibility(
                    visible = !selecting,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    NavigationBar {
                        Tab.entries.forEach { t ->
                            NavigationBarItem(
                                selected = tab == t,
                                onClick = {
                                    if (tab != t) vm.openAlbum(null)
                                    tab = t
                                },
                                icon = { Icon(t.icon, contentDescription = t.label) },
                                label = { Text(t.label) },
                            )
                        }
                    }
                }
            },
        ) { inner ->
            val content = Modifier.padding(PaddingValues(bottom = inner.calculateBottomPadding()))
            val gated = state.permissionGranted == false
            when (tab) {
                Tab.PHOTOS ->
                    if (gated) PermissionScreen(onRequestPermission, content)
                    else MediaBrowser(
                        title = "Photos",
                        scopeItems = state.all,
                        state = state,
                        vm = vm,
                        onBack = null,
                        onSettings = { overlay = Overlay.SETTINGS },
                        onAbout = { overlay = Overlay.ABOUT },
                        modifier = content,
                    )

                Tab.ALBUMS ->
                    if (gated) PermissionScreen(onRequestPermission, content)
                    else {
                        val album = state.openAlbum
                        if (album != null) {
                            MediaBrowser(
                                title = album.name,
                                scopeItems = album.items,
                                state = state,
                                vm = vm,
                                onBack = { vm.openAlbum(null) },
                                onSettings = { overlay = Overlay.SETTINGS },
                                onAbout = { overlay = Overlay.ABOUT },
                                modifier = content,
                            )
                        } else {
                            AlbumsScreen(
                                albums = state.albums,
                                loading = state.loading,
                                onOpen = vm::openAlbum,
                                onSettings = { overlay = Overlay.SETTINGS },
                                onAbout = { overlay = Overlay.ABOUT },
                                modifier = content,
                            )
                        }
                    }

                Tab.SEARCH -> SearchScreen(state, vm, content)
            }
        }

        val surface = MaterialTheme.colorScheme.surface
        when (overlay) {
            Overlay.SETTINGS -> Box(Modifier.fillMaxSize().background(surface)) {
                SettingsScreen(state, vm, onBack = { overlay = null }, onAbout = { overlay = Overlay.ABOUT })
            }
            Overlay.ABOUT -> Box(Modifier.fillMaxSize().background(surface)) { AboutScreen(onBack = { overlay = null }) }
            null -> Unit
        }

        state.viewer?.let { session ->
            ViewerScreen(session = session, onClose = vm::closeViewer)
        }
    }
}
