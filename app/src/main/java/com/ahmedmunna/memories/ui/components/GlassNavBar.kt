package com.ahmedmunna.memories.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

data class GlassNavItem(val label: String, val icon: ImageVector)

private val ItemWidth = 64.dp
private val ItemHeight = 52.dp

/**
 * Floating, icon-only navigation pill with a "liquid glass" look: a translucent tinted body,
 * a bright top edge and a faint bottom edge (the specular rim), a soft shadow, and a glass
 * bubble that glides between tabs on a spring.
 *
 * Note: this is translucency, not a true blur of the photos behind it.
 */
@Composable
fun GlassNavBar(
    items: List<GlassNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.surface.luminance() < 0.5f
    val shape = RoundedCornerShape(34.dp)

    val body = remember(colors.surface) {
        Brush.verticalGradient(
            listOf(
                colors.surface.copy(alpha = if (dark) 0.62f else 0.70f),
                colors.surface.copy(alpha = if (dark) 0.50f else 0.58f),
            ),
        )
    }
    val rim = Brush.verticalGradient(
        listOf(Color.White.copy(alpha = if (dark) 0.40f else 0.90f), Color.White.copy(alpha = if (dark) 0.06f else 0.20f)),
    )

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, rim),
    ) {
        Box(Modifier.background(body).padding(8.dp)) {
            val indicatorX by animateDpAsState(
                targetValue = ItemWidth * selectedIndex,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
                label = "navIndicator",
            )
            // The sliding glass bubble behind the selected icon.
            Box(
                Modifier
                    .offset(x = indicatorX)
                    .size(ItemWidth, ItemHeight)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.primary.copy(alpha = if (dark) 0.42f else 0.26f),
                                colors.primary.copy(alpha = if (dark) 0.24f else 0.12f),
                            ),
                        ),
                    ),
            )
            Row {
                items.forEachIndexed { i, item ->
                    val selected = i == selectedIndex
                    val tint by animateColorAsState(
                        if (selected) colors.primary else colors.onSurfaceVariant,
                        label = "navTint",
                    )
                    val scale by animateFloatAsState(
                        if (selected) 1.12f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
                        label = "navScale",
                    )
                    Box(
                        Modifier
                            .width(ItemWidth)
                            .height(ItemHeight)
                            .clip(RoundedCornerShape(26.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab,
                                onClickLabel = item.label,
                            ) { onSelect(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(26.dp).scale(scale),
                        )
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
