package com.example.herodtime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xFF3B82F6),
    primaryVariant = Color(0xFF1E40AF),
    secondary = Color(0xFF14B8A6),
    background = Color(0xFF111827),
    surface = Color(0xFF111827),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun HerodTimeTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else DarkColorPalette

    MaterialTheme(
        colors = colors,
        typography = androidx.compose.material.Typography(),
        shapes = androidx.compose.material.Shapes(),
        content = content
    )
}
