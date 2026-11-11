package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TvDarkColorScheme = darkColorScheme(
    primary = TvAccentGold,
    onPrimary = TvMidnightBackground,
    primaryContainer = Color(0xFF451A03), // Dark gold/brown
    onPrimaryContainer = Color(0xFFFFE082),
    secondary = TvAccentGold,
    onSecondary = Color(0xFF451A03),
    background = TvMidnightBackground,
    onBackground = TvTextPrimary,
    surface = TvMidnightBackground,
    onSurface = TvTextPrimary,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TvTextSecondary,
    outline = TvFocusBorder,
    error = TvAccentRed
)

@Composable
fun MyApplicationTheme(
    isArabic: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        typography = if (isArabic) ArabicCairoTypography else DefaultTypography,
        content = content
    )
}
