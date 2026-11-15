package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    isArabic: Boolean = false,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context).copy(
            background = TvMidnightBackground,
            surface = TvMidnightBackground
        )
    } else {
        TvDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = if (isArabic) ArabicCairoTypography else DefaultTypography,
        shapes = AuraShapes,
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun MyApplicationThemePreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Text(
                text = "Aura TV",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
