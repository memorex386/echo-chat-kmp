package com.echochat.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A fixed palette rather than dynamic colour, so the app looks the same on every device.
// Echo's avatar uses the tertiary colours, so it never reads as one of my messages.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0E6B5C),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF5FAF8),
    onBackground = Color(0xFF161D1B),
    surface = Color(0xFFF5FAF8),
    onSurface = Color(0xFF161D1B),
    onSurfaceVariant = Color(0xFF3F4946),
    surfaceContainerHigh = Color(0xFFE1E9E6),
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF6E3900),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7ED2BE),
    onPrimary = Color(0xFF00382F),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFDDE4E1),
    surface = Color(0xFF0F1513),
    onSurface = Color(0xFFDDE4E1),
    onSurfaceVariant = Color(0xFFBEC9C5),
    surfaceContainerHigh = Color(0xFF252B29),
    tertiaryContainer = Color(0xFF6E3900),
    onTertiaryContainer = Color(0xFFFFDCC2),
)

@Composable
fun EchoChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
