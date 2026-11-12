package com.example.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import android.os.Build
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.theme.TvMidnightBackground

data class AuroraColors(
    val glow: Color = Color(0xFF38BDF8),   // Vibrant Cyan/Sky Blue
    val mid: Color = Color(0xFF2563EB),    // Rich Royal Blue
    val accent: Color = Color(0xFF9333EA), // Vivid Purple/Violet
    val deep: Color = Color(0xFF1E1B4B),   // Deep Indigo/Navy
    val base: Color = Color(0xFF020617),   // Rich Obsidian Black
    val teal: Color = Color(0xFF0D9488),   // Luminous Teal
    val magenta: Color = Color(0xFFDB2777),// Cinematic Magenta Glow
    val amber: Color = Color(0xFFD97706)   // Subtle Warm Amber Highlight
)

fun Modifier.safeBlur(radius: Dp, edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle): Modifier {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.blur(radius, edgeTreatment)
        } else {
            this
        }
    } catch (_: Throwable) {
        this
    }
}

@Composable
fun AuroraBackground(
    target: AuroraColors = AuroraColors(),
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null
) {
    val spec = remember { tween<Color>(280) }
    val glow by animateColorAsState(target.glow, spec, label = "glow")
    val teal by animateColorAsState(target.teal, spec, label = "teal")
    val mid by animateColorAsState(target.mid, spec, label = "mid")
    val accent by animateColorAsState(target.accent, spec, label = "accent")
    val deep by animateColorAsState(target.deep, spec, label = "deep")
    val base by animateColorAsState(target.base, spec, label = "base")
    val magenta by animateColorAsState(target.magenta, spec, label = "magenta")
    val amber by animateColorAsState(target.amber, spec, label = "amber")

    // High-performance precomputed blue-noise dither shader (cached once, runs via GPU repeat mode)
    // Eliminates color banding (خطوط تدرج الألوان) completely on all TV screen panels
    val ditherShaderBrush = remember {
        val size = 64
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val rand = java.util.Random(1337)
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            val lum = rand.nextInt(256)
            val alpha = rand.nextInt(7) + 1 // Subtle alpha (1..7 / 255) to smoothly break color quantization steps
            pixels[i] = android.graphics.Color.argb(alpha, lum, lum, lum)
        }
        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        ShaderBrush(BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT))
    }

    Box(modifier = modifier.fillMaxSize().background(base)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .safeBlur(radius = 28.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            val w = size.width
            val h = size.height

            // 1) Seamless multi-stop smooth linear base gradient (no harsh color jumps)
            drawRect(
                brush = Brush.linearGradient(
                    0.00f to deep.copy(alpha = 0.60f),
                    0.20f to deep.copy(alpha = 0.45f),
                    0.40f to accent.copy(alpha = 0.30f),
                    0.65f to base.copy(alpha = 0.65f),
                    0.85f to base.copy(alpha = 0.88f),
                    1.00f to base,
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )

            // 2) Middle-left rich cobalt/sapphire bloom with harmonic color stops
            drawRect(
                brush = Brush.radialGradient(
                    0.00f to mid.copy(alpha = 0.80f),
                    0.25f to mid.copy(alpha = 0.55f),
                    0.50f to deep.copy(alpha = 0.35f),
                    0.75f to deep.copy(alpha = 0.12f),
                    1.00f to Color.Transparent,
                    center = Offset(w * 0.22f, h * 0.42f),
                    radius = w * 0.95f
                )
            )

            // 3) Upper-right luminous Teal & Cyan atmospheric beam
            withTransform({ scale(1.1f, 0.50f, pivot = Offset(w * 0.75f, h * 0.15f)) }) {
                drawRect(
                    brush = Brush.radialGradient(
                        0.00f to teal.copy(alpha = 0.75f),
                        0.25f to teal.copy(alpha = 0.50f),
                        0.50f to glow.copy(alpha = 0.30f),
                        0.75f to teal.copy(alpha = 0.10f),
                        1.00f to Color.Transparent,
                        center = Offset(w * 0.75f, h * 0.15f),
                        radius = w * 0.80f
                    )
                )
            }

            // 4) Top-center luminous Cyan hotspot band
            withTransform({ scale(1.0f, 0.45f, pivot = Offset(w * 0.35f, h * 0.12f)) }) {
                drawRect(
                    brush = Brush.radialGradient(
                        0.00f to Color.White.copy(alpha = 0.40f),
                        0.20f to glow.copy(alpha = 0.70f),
                        0.45f to mid.copy(alpha = 0.42f),
                        0.70f to accent.copy(alpha = 0.18f),
                        0.90f to deep.copy(alpha = 0.06f),
                        1.00f to Color.Transparent,
                        center = Offset(w * 0.35f, h * 0.12f),
                        radius = w * 0.85f
                    )
                )
            }

            // 5) Right-side professional Magenta & Purple glow streak
            withTransform({ scale(1.2f, 0.45f, pivot = Offset(w * 0.82f, h * 0.35f)) }) {
                drawRect(
                    brush = Brush.radialGradient(
                        0.00f to magenta.copy(alpha = 0.70f),
                        0.25f to magenta.copy(alpha = 0.48f),
                        0.55f to accent.copy(alpha = 0.26f),
                        0.80f to magenta.copy(alpha = 0.08f),
                        1.00f to Color.Transparent,
                        center = Offset(w * 0.82f, h * 0.35f),
                        radius = w * 0.70f
                    )
                )
            }

            // 6) Bottom-left warm Amber/Gold subtle atmospheric touch for cinematic depth
            withTransform({ scale(1.0f, 0.50f, pivot = Offset(w * 0.15f, h * 0.88f)) }) {
                drawRect(
                    brush = Brush.radialGradient(
                        0.00f to amber.copy(alpha = 0.35f),
                        0.30f to amber.copy(alpha = 0.20f),
                        0.60f to accent.copy(alpha = 0.10f),
                        1.00f to Color.Transparent,
                        center = Offset(w * 0.15f, h * 0.88f),
                        radius = w * 0.60f
                    )
                )
            }

            // 7) Deep cinematic top vignette
            drawRect(
                brush = Brush.verticalGradient(
                    0.00f to Color.Black.copy(alpha = 0.70f),
                    0.08f to Color.Black.copy(alpha = 0.32f),
                    0.15f to Color.Black.copy(alpha = 0.10f),
                    0.22f to Color.Transparent,
                    startY = 0f,
                    endY = h * 0.25f
                )
            )

            // 8) Bottom-right corner rich vignette
            drawRect(
                brush = Brush.radialGradient(
                    0.00f to Color.Black.copy(alpha = 0.88f),
                    0.35f to Color.Black.copy(alpha = 0.60f),
                    0.65f to Color.Black.copy(alpha = 0.30f),
                    0.85f to Color.Black.copy(alpha = 0.08f),
                    1.00f to Color.Transparent,
                    center = Offset(w * 1.05f, h * 1.05f),
                    radius = w * 0.65f
                )
            )
        }

        // Fast GPU dithering layer to break gradient color steps smoothly without any lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(ditherShaderBrush)
        }

        content?.invoke()
    }
}

@Composable
fun AuroraBackground(
    colors: AuroraColors,
    modifier: Modifier = Modifier
) {
    AuroraBackground(target = colors, modifier = modifier, content = null)
}

// Extract colors from app icon using Palette & HSL shifts
fun auroraFromIcon(bitmap: Bitmap): AuroraColors {
    val p = Palette.from(bitmap).generate()
    val src = p.vibrantSwatch?.rgb
        ?: p.dominantSwatch?.rgb
        ?: p.lightVibrantSwatch?.rgb
        ?: p.darkVibrantSwatch?.rgb
        ?: p.mutedSwatch?.rgb
        ?: return AuroraColors()

    fun shift(h: Float, l: Float? = null, s: Float? = null): Color {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(src, hsl)
        hsl[0] = (hsl[0] + h + 360f) % 360f
        l?.let { hsl[2] = it }
        s?.let { hsl[1] = it }
        return Color(ColorUtils.HSLToColor(hsl))
    }

    return AuroraColors(
        glow = shift(0f, l = 0.58f, s = 0.95f),
        mid = shift(-25f, l = 0.48f, s = 0.85f),
        accent = shift(70f, l = 0.40f, s = 0.75f),
        deep = shift(-10f, l = 0.22f, s = 0.80f),
        base = shift(0f, l = 0.02f, s = 0.40f),
        teal = shift(25f, l = 0.40f, s = 0.75f),
    )
}


@Composable
fun DynamicBackground(
    targetColorInt: Int? = null,
    auroraColors: AuroraColors = AuroraColors(),
    isEnabled: Boolean = true,
    wallpaperMode: String = "dynamic",
    customWallpaperUrl: String = "",
    content: @Composable () -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvMidnightBackground)
    ) {
        // Mirrored Background Layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (isRtl) {
                        scaleX = -1f
                    }
                }
        ) {
            if ((wallpaperMode == "custom" || wallpaperMode == "preset") && customWallpaperUrl.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = customWallpaperUrl,
                        contentDescription = "Active Wallpaper",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Ambient vignette overlay for legibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Black.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )
                }
            } else if (isEnabled) {
                AuroraBackground(colors = auroraColors)
            }
        }

        // App Content Layer (Outside mirrored box)
        content()
    }
}
