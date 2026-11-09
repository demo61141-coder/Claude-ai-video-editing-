package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioDarkColorScheme = darkColorScheme(
    primary = BroadcastCrimson,
    onPrimary = Color.White,
    primaryContainer = BroadcastCrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = BroadcastAmber,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF3B2806),
    onSecondaryContainer = BroadcastAmber,
    tertiary = BroadcastEmerald,
    onTertiary = Color(0xFF052E16),
    background = StudioObsidian,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardBorder
)

@Composable
fun BanglaNewsStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
