package com.ahmedmunna.memories.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp

/** The Memories mark: a sun over two peaks on an indigo tile. Drawn in code so it scales cleanly. */
@Composable
fun MemoriesLogo(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color(0xFF6D63FF), Color(0xFF4F46E5), Color(0xFF312E81)),
                start = Offset.Zero,
                end = Offset(s, s),
            ),
            cornerRadius = CornerRadius(s * 0.26f),
        )
        drawCircle(Color(0xFFFFD166), radius = s * 0.085f, center = Offset(s * 0.66f, s * 0.33f))
        val back = Path().apply {
            moveTo(s * 0.44f, s * 0.72f); lineTo(s * 0.60f, s * 0.46f); lineTo(s * 0.82f, s * 0.72f); close()
        }
        drawPath(back, Color(0xFFC7D2FE))
        val front = Path().apply {
            moveTo(s * 0.20f, s * 0.72f); lineTo(s * 0.41f, s * 0.38f); lineTo(s * 0.62f, s * 0.72f); close()
        }
        drawPath(front, Color.White)
    }
}
