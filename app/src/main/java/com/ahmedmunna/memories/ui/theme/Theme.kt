package com.ahmedmunna.memories.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.ahmedmunna.memories.data.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E0FF),
    onPrimaryContainer = Color(0xFF16134F),
    secondary = Color(0xFF5C5D72),
    tertiary = Color(0xFFB4538A),
    background = Color(0xFFFBF8FF),
    surface = Color(0xFFFBF8FF),
    surfaceVariant = Color(0xFFE4E1EC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC0C1FF),
    onPrimary = Color(0xFF1B1A6B),
    primaryContainer = Color(0xFF3A3A9E),
    onPrimaryContainer = Color(0xFFE0E0FF),
    secondary = Color(0xFFC5C4DD),
    tertiary = Color(0xFFFFAED8),
    background = Color(0xFF121218),
    surface = Color(0xFF121218),
    surfaceVariant = Color(0xFF47464F),
)

@Composable
fun MemoriesTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
