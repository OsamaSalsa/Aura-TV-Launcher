package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TvAccentGold

/**
 * Glass Frame Overlay (برواز زجاجي عاكس يحيط بالأيقونات والبرامج)
 */
@Composable
fun GlassSheenOverlay(
    isFocused: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp),
    glassShadowAlpha: Float = 0f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
    ) {
        // 1. Top Glass Highlight Line (حافة علوية عاكسة للضوء)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = if (isFocused) 0.85f else 0.45f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    )
                )
        )

        // 2. Beveled Glass Frame Border (برواز زجاجي مشطوف ثلاثي الأبعاد)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isFocused) 2.5.dp else 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isFocused) {
                            listOf(
                                TvAccentGold,
                                Color.White,
                                TvAccentGold.copy(alpha = 0.60f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        }
                    ),
                    shape = shape
                )
        )
    }
}

@Composable
fun FrostedGlassTile(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp),
    opacityPercent: Int = 75,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        content()
        GlassSheenOverlay(isFocused = isFocused, shape = shape)
    }
}
