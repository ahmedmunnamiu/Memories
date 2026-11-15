package com.ahmedmunna.memories.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ahmedmunna.memories.data.GridEntry
import com.ahmedmunna.memories.data.MediaItem
import com.ahmedmunna.memories.util.formatDuration

@Composable
fun MediaGrid(
    entries: List<GridEntry>,
    columns: Int,
    selection: Set<Long>,
    onColumnsChange: (Int) -> Unit,
    onClick: (MediaItem) -> Unit,
    onLongClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val currentColumns by rememberUpdatedState(columns)
    val currentChange by rememberUpdatedState(onColumnsChange)
    val selecting = selection.isNotEmpty()

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.pinchToChangeColumns({ currentColumns }, { currentChange(it) }),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(
            items = entries,
            key = { if (it is GridEntry.Header) it.key else "m${(it as GridEntry.Media).item.id}" },
            span = { if (it is GridEntry.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1) },
            contentType = { if (it is GridEntry.Header) 0 else 1 },
        ) { entry ->
            when (entry) {
                is GridEntry.Header -> Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
                )
                is GridEntry.Media -> MediaCell(
                    item = entry.item,
                    selected = entry.item.id in selection,
                    selecting = selecting,
                    onClick = { onClick(entry.item) },
                    onLongClick = { onLongClick(entry.item) },
                )
            }
        }
    }
}

/**
 * Two-finger pinch changes grid density (spread = bigger thumbnails, pinch = more columns).
 * Runs in the Initial pass and only consumes when two fingers are down, so normal scrolling is untouched.
 */
private fun Modifier.pinchToChangeColumns(get: () -> Int, set: (Int) -> Unit): Modifier =
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var accumulated = 1f
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.count { it.pressed } >= 2) {
                    accumulated *= event.calculateZoom()
                    if (accumulated > 1.3f) {
                        set((get() - 1).coerceAtLeast(2)); accumulated = 1f
                    } else if (accumulated < 0.77f) {
                        set((get() + 1).coerceAtMost(5)); accumulated = 1f
                    }
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                }
            } while (event.changes.any { it.pressed })
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaCell(
    item: MediaItem,
    selected: Boolean,
    selecting: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(
        Modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        val inset by animateDpAsState(if (selected) 10.dp else 0.dp, label = "selectInset")
        AsyncImage(
            model = item.uri,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(inset)
                .clip(RoundedCornerShape(if (selected) 10.dp else 0.dp)),
        )

        if (item.isVideo) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.65f to Color.Transparent, 1f to Color(0x99000000))),
            )
            Row(
                Modifier.align(Alignment.BottomEnd).padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(formatDuration(item.durationMs), color = Color.White, fontSize = 11.sp)
            }
        } else if (item.isGif) {
            Text(
                "GIF",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color(0x99000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }

        if (selecting) {
            Icon(
                imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (selected) "Selected" else "Not selected",
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp).size(22.dp),
            )
        }
    }
}
