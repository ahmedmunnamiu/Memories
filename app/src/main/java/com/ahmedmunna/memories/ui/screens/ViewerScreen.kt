package com.ahmedmunna.memories.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ahmedmunna.memories.data.MediaItem
import com.ahmedmunna.memories.ui.ViewerSession
import com.ahmedmunna.memories.util.MediaActions
import com.ahmedmunna.memories.util.formatBytes
import com.ahmedmunna.memories.util.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(session: ViewerSession, onClose: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = session.startIndex.coerceIn(0, session.items.lastIndex)) {
        session.items.size
    }
    var chromeVisible by remember { mutableStateOf(true) }
    var zoomed by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var showInfo by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val current = session.items[pagerState.currentPage]

    BackHandler(onBack = onClose)
    LaunchedEffect(pagerState.currentPage) { zoomed = false }

    val trashLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) onClose()
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 1f - dragFraction * 0.85f))) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !zoomed,
            key = { session.items[it].id },
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val item = session.items[page]
            if (item.isVideo) {
                VideoPage(item, isCurrent = page == pagerState.currentPage)
            } else {
                ZoomableImage(
                    item = item,
                    onTap = { chromeVisible = !chromeVisible },
                    onZoomChanged = { if (page == pagerState.currentPage) zoomed = it },
                    onDragFraction = { dragFraction = it },
                    onClose = onClose,
                )
            }
        }

        AnimatedVisibility(
            visible = chromeVisible && dragFraction == 0f,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xB3000000), Color.Transparent)))
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close", tint = Color.White) }
                Column(Modifier.weight(1f)) {
                    Text(current.name, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    Text(
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(current.takenMs)),
                        color = Color(0xCCFFFFFF),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    "${pagerState.currentPage + 1} / ${session.items.size}",
                    color = Color(0xCCFFFFFF),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = chromeVisible && dragFraction == 0f,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB3000000))))
                    .navigationBarsPadding()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = { MediaActions.share(context, listOf(current)) }) {
                    Icon(Icons.Filled.Share, "Share", tint = Color.White)
                }
                IconButton(onClick = { showInfo = true }) { Icon(Icons.Filled.Info, "Details", tint = Color.White) }
                IconButton(onClick = {
                    val sender = MediaActions.trashRequest(context, listOf(current.uri))
                    if (sender == null) {
                        Toast.makeText(context, "Moving to trash needs Android 11 or newer", Toast.LENGTH_LONG).show()
                    } else {
                        trashLauncher.launch(IntentSenderRequest.Builder(sender).build())
                    }
                }) { Icon(Icons.Filled.Delete, "Move to trash", tint = Color.White) }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "More", tint = Color.White) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Open with…") }, onClick = {
                            menu = false; MediaActions.openWith(context, current)
                        })
                        if (!current.isVideo) {
                            DropdownMenuItem(text = { Text("Set as wallpaper") }, onClick = {
                                menu = false; MediaActions.setAsWallpaper(context, current)
                            })
                        }
                    }
                }
            }
        }
    }

    if (showInfo) {
        ModalBottomSheet(onDismissRequest = { showInfo = false }) { InfoSheet(current) }
    }
}

/**
 * Pinch / double-tap zoom, pan while zoomed, and swipe-down-to-close at 1x.
 * Only consumes pointer events when it is actually using them, so the pager can still swipe sideways.
 */
@Composable
private fun ZoomableImage(
    item: MediaItem,
    onTap: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    onDragFraction: (Float) -> Unit,
    onClose: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val slop = LocalViewConfiguration.current.touchSlop
    val context = LocalContext.current

    fun clamp(o: Offset, s: Float): Offset {
        val maxX = size.width * (s - 1f) / 2f
        val maxY = size.height * (s - 1f) / 2f
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    LaunchedEffect(scale > 1.01f) { onZoomChanged(scale > 1.01f) }

    Box(
        Modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { tap ->
                        if (scale > 1.01f) {
                            scale = 1f; offset = Offset.Zero
                        } else {
                            scale = 2.5f
                            offset = clamp(
                                Offset(size.width / 2f - tap.x, size.height / 2f - tap.y) * (2.5f - 1f),
                                2.5f,
                            )
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dismissing = false
                    do {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount >= 2) {
                            scale = (scale * event.calculateZoom()).coerceIn(1f, 6f)
                            offset = clamp(offset + event.calculatePan(), scale)
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } else if (pressedCount == 1) {
                            val change = event.changes.first { it.pressed }
                            if (scale > 1.01f) {
                                val pan = event.calculatePan()
                                if (pan != Offset.Zero) {
                                    offset = clamp(offset + pan, scale)
                                    change.consume()
                                }
                            } else {
                                val dy = change.position.y - down.position.y
                                val dx = change.position.x - down.position.x
                                if (!dismissing && dy > slop && dy > abs(dx) * 1.5f) dismissing = true
                                if (dismissing) {
                                    dragY = dy.coerceAtLeast(0f)
                                    onDragFraction((dragY / size.height.coerceAtLeast(1)).coerceIn(0f, 1f))
                                    change.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (scale < 1.05f) { scale = 1f; offset = Offset.Zero }
                    if (dismissing) {
                        if (dragY > size.height * 0.18f) {
                            onClose()
                        } else {
                            dragY = 0f
                            onDragFraction(0f)
                        }
                    }
                }
            },
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(item.uri).size(2560).build(),
            contentDescription = item.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y + dragY
                },
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoPage(item: MediaItem, isCurrent: Boolean) {
    val context = LocalContext.current
    val player = remember(item.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(item.uri))
            prepare()
        }
    }
    LaunchedEffect(isCurrent) { player.playWhenReady = isCurrent; if (!isCurrent) player.pause() }
    DisposableEffect(player) { onDispose { player.release() } }

    AndroidView(
        factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true } },
        update = { it.player = player },
        modifier = Modifier.fillMaxSize(),
    )
}

private data class ExifDetails(
    val camera: String?,
    val exposure: String?,
    val lat: Double?,
    val lon: Double?,
)

private suspend fun readExif(context: Context, item: MediaItem): ExifDetails? = withContext(Dispatchers.IO) {
    if (item.isVideo) return@withContext null
    try {
        val canReadLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_MEDIA_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        // Without ACCESS_MEDIA_LOCATION the system strips GPS from the stream; with it we ask for the original.
        val uri: Uri = if (canReadLocation) MediaStore.setRequireOriginal(item.uri) else item.uri
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            val make = exif.getAttribute(ExifInterface.TAG_MAKE)
            val model = exif.getAttribute(ExifInterface.TAG_MODEL)
            val parts = listOfNotNull(
                exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" },
                exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { "${it}s" },
                exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)?.let { "ISO $it" },
                exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "focal $it mm" },
            )
            val ll = exif.latLong
            ExifDetails(
                camera = listOfNotNull(make, model).joinToString(" ").ifBlank { null },
                exposure = parts.joinToString("  ·  ").ifBlank { null },
                lat = ll?.get(0),
                lon = ll?.get(1),
            )
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun InfoSheet(item: MediaItem) {
    val context = LocalContext.current
    val exif by produceState<ExifDetails?>(initialValue = null, item.id) { value = readExif(context, item) }

    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text("Details", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))
        InfoRow("File name", item.name)
        InfoRow("Captured", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(Date(item.takenMs)))
        if (item.width > 0 && item.height > 0) {
            val mp = item.width.toLong() * item.height / 1_000_000.0
            InfoRow("Resolution", "${item.width} × ${item.height}  (${String.format("%.1f", mp)} MP)")
        }
        InfoRow("Size", formatBytes(item.sizeBytes))
        InfoRow("Format", "${item.format}  (${item.mimeType})")
        if (item.isVideo && item.durationMs > 0) InfoRow("Duration", formatDuration(item.durationMs))
        if (item.relativePath.isNotBlank()) InfoRow("Location on device", item.relativePath)
        exif?.camera?.let { InfoRow("Camera", it) }
        exif?.exposure?.let { InfoRow("Exposure", it) }
        val lat = exif?.lat
        val lon = exif?.lon
        if (lat != null && lon != null) {
            InfoRow("GPS", String.format("%.5f, %.5f", lat, lon))
            TextButton(onClick = { MediaActions.openMap(context, lat, lon) }) { Text("Open in maps") }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
