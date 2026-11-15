package com.ahmedmunna.memories.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedmunna.memories.data.Album
import com.ahmedmunna.memories.data.AppSettings
import com.ahmedmunna.memories.data.MediaFilter
import com.ahmedmunna.memories.data.MediaItem
import com.ahmedmunna.memories.data.MediaOrganizer
import com.ahmedmunna.memories.data.MediaRepository
import com.ahmedmunna.memories.data.SettingsRepository
import com.ahmedmunna.memories.data.SortOption
import com.ahmedmunna.memories.data.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewerSession(val items: List<MediaItem>, val startIndex: Int)

data class GalleryUiState(
    /** null until the first permission check finishes. */
    val permissionGranted: Boolean? = null,
    val loading: Boolean = true,
    val all: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val filter: MediaFilter = MediaFilter.ALL,
    val selection: Set<Long> = emptySet(),
    val openAlbumId: Long? = null,
    val viewer: ViewerSession? = null,
) {
    val openAlbum: Album? get() = albums.firstOrNull { it.id == openAlbumId }
}

private data class DataState(
    val permission: Boolean?,
    val loading: Boolean,
    val media: List<MediaItem>,
    val settings: AppSettings,
)

private data class MediaState(val loading: Boolean, val media: List<MediaItem>)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GalleryViewModel @Inject constructor(
    repository: MediaRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val permission = MutableStateFlow<Boolean?>(null)
    private val filter = MutableStateFlow(MediaFilter.ALL)
    private val selection = MutableStateFlow<Set<Long>>(emptySet())
    private val openAlbumId = MutableStateFlow<Long?>(null)
    private val viewer = MutableStateFlow<ViewerSession?>(null)

    private val mediaState = permission.flatMapLatest { granted ->
        if (granted == true) repository.observeMedia().map { MediaState(false, it) }
        else flowOf(MediaState(loading = granted == null, media = emptyList()))
    }

    private val data = combine(permission, mediaState, settingsRepository.settings) { p, m, s ->
        DataState(p, m.loading, m.media, s)
    }

    val ui: StateFlow<GalleryUiState> = combine(data, filter, selection, openAlbumId, viewer) { d, f, sel, album, v ->
        GalleryUiState(
            permissionGranted = d.permission,
            loading = d.loading,
            all = d.media,
            albums = MediaOrganizer.buildAlbums(d.media),
            settings = d.settings,
            filter = f,
            selection = sel,
            openAlbumId = album,
            viewer = v,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GalleryUiState())

    fun setPermission(granted: Boolean) { permission.value = granted }
    fun setFilter(f: MediaFilter) { filter.value = f }

    fun toggleSelect(id: Long) {
        selection.value = selection.value.let { if (id in it) it - id else it + id }
    }
    fun selectAll(ids: Collection<Long>) { selection.value = ids.toSet() }
    fun clearSelection() { selection.value = emptySet() }

    fun openAlbum(id: Long?) {
        selection.value = emptySet()
        filter.value = MediaFilter.ALL
        openAlbumId.value = id
    }

    fun openViewer(items: List<MediaItem>, index: Int) { viewer.value = ViewerSession(items, index) }
    fun closeViewer() { viewer.value = null }

    fun setColumns(c: Int) = viewModelScope.launch { settingsRepository.setColumns(c) }
    fun setSort(s: SortOption) = viewModelScope.launch { settingsRepository.setSort(s) }
    fun setSortDescending(d: Boolean) = viewModelScope.launch { settingsRepository.setSortDescending(d) }
    fun setTheme(m: ThemeMode) = viewModelScope.launch { settingsRepository.setTheme(m) }
    fun setDynamicColor(e: Boolean) = viewModelScope.launch { settingsRepository.setDynamicColor(e) }
}
