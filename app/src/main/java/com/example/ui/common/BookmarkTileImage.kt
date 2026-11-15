package com.example.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Renders a Web Bookmark's resolved visual:
 * - Wide social meta images (e.g. og:image, twitter:image with aspect >= 1.35) fill the full TV card with Crop.
 * - Square or vertical browser icons (favicon / apple-touch-icon) render with ContentScale.Fit over a subtle
 *   ambient backdrop so the full height of the icon is always visible without top/bottom cropping.
 */
@Composable
fun BookmarkTileImage(
    iconUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconVerticalPadding: Dp = 5.dp,
    iconHorizontalPadding: Dp = 8.dp,
    loadingContent: @Composable () -> Unit = {},
    errorContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val imageData = remember(iconUrl) {
        if (iconUrl.startsWith("/")) File(iconUrl) else iconUrl
    }

    SubcomposeAsyncImage(
        model = remember(imageData) {
            ImageRequest.Builder(context)
                .data(imageData)
                .crossfade(true)
                .build()
        },
        contentDescription = contentDescription,
        modifier = modifier,
        loading = { loadingContent() },
        error = { errorContent() },
        success = { state ->
            val drawable = state.result.drawable
            val dw = drawable.intrinsicWidth.toFloat()
            val dh = drawable.intrinsicHeight.toFloat()
            val isWideBanner = dw > 0f && dh > 0f && (dw / dh) >= 1.35f

            if (isWideBanner) {
                Image(
                    painter = state.painter,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF131E36), Color(0xFF0A101E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = state.painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.16f,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )
                    Image(
                        painter = state.painter,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = iconVerticalPadding, horizontal = iconHorizontalPadding)
                    )
                }
            }
        }
    )
}
