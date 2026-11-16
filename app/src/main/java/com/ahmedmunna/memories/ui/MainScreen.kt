package com.ahmedmunna.memories.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Alignment
import com.ahmedmunna.memories.ui.components.GlassNavBar
import com.ahmedmunna.memories.ui.components.GlassNavItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

    // Bottom bar hides while scrolling down through the library and returns on scrolling back up.
    var barVisible by remember { mutableStateOf(true) }
    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -8f) barVisible = false
                else if (available.y > 8f) barVisible = true
                return Offset.Zero
            }
        }
    }

    // Back priority: viewer > overlay > selection > open album.
    BackHandler(enabled = state.viewer == null && overlay != null) { overlay = null }
    BackHandler(enabled = state.viewer == null && overlay == null && selecting) { vm.clearSelection() }
    BackHandler(enabled = state.viewer == null && overlay == null && !selecting && state.openAlbumId != null) {
        vm.openAlbum(null)
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val content = Modifier.fillMaxSize().nestedScroll(scrollConnection)
        val gated = state.permissionGranted == false

        // Tabs slide and fade in the direction of travel.
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (fadeIn(tween(260)) + slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { dir * it / 8 }) togetherWith
                    (fadeOut(tween(180)) + slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { -dir * it / 8 })
            },
            label = "tabs",
        ) { currentTab ->
            when (currentTab) {
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
                    else AnimatedContent(
                        targetState = state.openAlbumId,
                        transitionSpec = {
                            val opening = targetState != null
                            (fadeIn(tween(260)) + slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { if (opening) it / 5 else -it / 5 }) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { if (opening) -it / 5 else it / 5 })
                        },
                        label = "albumOpen",
                    ) { albumId ->
                        val album = state.albums.firstOrNull { it.id == albumId }
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

        // Floating glass tab bar over the content.
        AnimatedVisibility(
            visible = !selecting && barVisible,
            enter = fadeIn(tween(220)) + slideInVertically(tween(300, easing = FastOutSlowInEasing)) { it },
            exit = fadeOut(tween(160)) + slideOutVertically(tween(260, easing = FastOutSlowInEasing)) { it },
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 14.dp),
        ) {
            GlassNavBar(
                items = Tab.entries.map { GlassNavItem(it.label, it.icon) },
                selectedIndex = tab.ordinal,
                onSelect = { i ->
                    val t = Tab.entries[i]
                    if (tab != t) { vm.openAlbum(null); barVisible = true }
                    tab = t
                },
            )
        }

        // Settings / About slide in over everything except the viewer.
        val surface = MaterialTheme.colorScheme.surface
        var lastOverlay by remember { mutableStateOf(Overlay.SETTINGS) }
        if (overlay != null) lastOverlay = overlay!!
        AnimatedVisibility(
            visible = overlay != null,
            enter = fadeIn(tween(220)) + slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { it / 3 },
            exit = fadeOut(tween(180)) + slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { it / 3 },
        ) {
            Box(Modifier.fillMaxSize().background(surface)) {
                when (lastOverlay) {
                    Overlay.SETTINGS -> SettingsScreen(state, vm, onBack = { overlay = null }, onAbout = { overlay = Overlay.ABOUT })
                    Overlay.ABOUT -> AboutScreen(onBack = { overlay = null })
                }
            }
        }

        // Keep the last session around so the viewer can animate out after the state is cleared.
        var lastSession by remember { mutableStateOf<ViewerSession?>(null) }
        if (state.viewer != null) lastSession = state.viewer
        AnimatedVisibility(
            visible = state.viewer != null,
            enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.88f, animationSpec = tween(280, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.88f, animationSpec = tween(220, easing = FastOutSlowInEasing)),
        ) {
            lastSession?.let { session ->
                ViewerScreen(session = session, onClose = vm::closeViewer)
            }
        }
    }
}
