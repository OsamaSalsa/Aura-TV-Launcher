package com.example.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import com.example.ui.theme.TvMidnightBackground
import kotlin.math.PI
import kotlin.math.cos

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

/**
 * Resamples sparse gradient keyframes into a dense C1-continuous cosine-eased table
 * so Skia hardware shaders never produce Mach bands or visible contour lines.
 */
private fun buildCosineGradientStops(
    keyframes: List<Pair<Float, Color>>,
    steps: Int = 14
): Pair<IntArray, FloatArray> {
    val colors = IntArray(steps)
    val positions = FloatArray(steps)
    val lastIdx = steps - 1
    for (i in 0 until steps) {
        val t = i.toFloat() / lastIdx.toFloat()
        positions[i] = t
        var segIdx = 0
        while (segIdx < keyframes.size - 2 && t > keyframes[segIdx + 1].first) {
            segIdx++
        }
        val (p0, c0) = keyframes[segIdx]
        val (p1, c1) = keyframes[segIdx + 1]
        val span = (p1 - p0).coerceAtLeast(0.0001f)
        val localT = ((t - p0) / span).coerceIn(0f, 1f)
        val easedT = 0.5f * (1f - cos(PI.toFloat() * localT))
        colors[i] = lerp(c0, c1, easedT).toArgb()
    }
    return colors to positions
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

    // Native Paint with hardware DITHER_FLAG enabled per gradient draw call
    val nativePaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
            isDither = true
            isFilterBitmap = true
        }
    }

    // Ultra-subtle balanced 128x128 triangular micro-dither paint to eliminate 8-bit HDMI/panel banding
    val ditherPaint = remember {
        val size = 128
        val bmp = createBitmap(size, size)
        val rand = java.util.Random(1337L)
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            // Triangular zero-mean noise: tiny +/- luma perturbation at 1..2 alpha
            val bright = rand.nextBoolean()
            val lum = if (bright) 255 else 0
            val alpha = 1 + rand.nextInt(2)
            pixels[i] = android.graphics.Color.argb(alpha, lum, lum, lum)
        }
        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        Paint(Paint.DITHER_FLAG).apply {
            isDither = true
            isFilterBitmap = false
            shader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(base)) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas

            drawIntoCanvas { canvas ->
                val nc = canvas.nativeCanvas

                // 1) Seamless multi-stop diagonal backdrop gradient
                val (baseCols, basePos) = buildCosineGradientStops(
                    listOf(
                        0.00f to deep.copy(alpha = 0.62f),
                        0.38f to accent.copy(alpha = 0.28f),
                        0.72f to base.copy(alpha = 0.78f),
                        1.00f to base
                    ),
                    steps = 16
                )
                nativePaint.shader = LinearGradient(
                    0f, 0f, w, h,
                    baseCols, basePos,
                    Shader.TileMode.CLAMP
                )
                nc.drawRect(0f, 0f, w, h, nativePaint)

                // 2) Middle-left rich cobalt/sapphire bloom (drawn as full radial circle — zero clipping)
                val (midCols, midPos) = buildCosineGradientStops(
                    listOf(
                        0.00f to mid.copy(alpha = 0.78f),
                        0.45f to deep.copy(alpha = 0.36f),
                        1.00f to deep.copy(alpha = 0f)
                    )
                )
                val midCx = w * 0.22f
                val midCy = h * 0.42f
                val midR = w * 0.95f
                nativePaint.shader = RadialGradient(
                    midCx, midCy, midR,
                    midCols, midPos,
                    Shader.TileMode.CLAMP
                )
                nc.drawCircle(midCx, midCy, midR, nativePaint)

                // 3) Upper-right luminous Teal & Cyan atmospheric beam (drawCircle inside scale prevents rect clipping lines)
                val (tealCols, tealPos) = buildCosineGradientStops(
                    listOf(
                        0.00f to teal.copy(alpha = 0.72f),
                        0.48f to glow.copy(alpha = 0.30f),
                        1.00f to teal.copy(alpha = 0f)
                    )
                )
                val tealCx = w * 0.75f
                val tealCy = h * 0.16f
                val tealR = w * 0.80f
                nativePaint.shader = RadialGradient(
                    tealCx, tealCy, tealR,
                    tealCols, tealPos,
                    Shader.TileMode.CLAMP
                )
                nc.save()
                nc.scale(1.10f, 0.54f, tealCx, tealCy)
                nc.drawCircle(tealCx, tealCy, tealR, nativePaint)
                nc.restore()

                // 4) Top-center luminous Cyan hotspot band (unclipped elliptical circle)
                val (cyanCols, cyanPos) = buildCosineGradientStops(
                    listOf(
                        0.00f to Color.White.copy(alpha = 0.32f),
                        0.24f to glow.copy(alpha = 0.64f),
                        0.55f to mid.copy(alpha = 0.34f),
                        0.80f to accent.copy(alpha = 0.12f),
                        1.00f to deep.copy(alpha = 0f)
                    ),
                    steps = 16
                )
                val cyanCx = w * 0.35f
                val cyanCy = h * 0.12f
                val cyanR = w * 0.85f
                nativePaint.shader = RadialGradient(
                    cyanCx, cyanCy, cyanR,
                    cyanCols, cyanPos,
                    Shader.TileMode.CLAMP
                )
                nc.save()
                nc.scale(1.05f, 0.48f, cyanCx, cyanCy)
                nc.drawCircle(cyanCx, cyanCy, cyanR, nativePaint)
                nc.restore()

                // 5) Right-side Magenta & Purple glow streak (unclipped elliptical circle)
                val (magCols, magPos) = buildCosineGradientStops(
                    listOf(
                        0.00f to magenta.copy(alpha = 0.66f),
                        0.50f to accent.copy(alpha = 0.28f),
                        1.00f to magenta.copy(alpha = 0f)
                    )
                )
                val magCx = w * 0.82f
                val magCy = h * 0.36f
                val magR = w * 0.72f
                nativePaint.shader = RadialGradient(
                    magCx, magCy, magR,
                    magCols, magPos,
                    Shader.TileMode.CLAMP
                )
                nc.save()
                nc.scale(1.20f, 0.48f, magCx, magCy)
                nc.drawCircle(magCx, magCy, magR, nativePaint)
                nc.restore()

                // 6) Bottom-left warm Amber/Gold subtle highlight (unclipped elliptical circle)
                val (ambCols, ambPos) = buildCosineGradientStops(
                    listOf(
                        0.00f to amber.copy(alpha = 0.34f),
                        0.55f to accent.copy(alpha = 0.10f),
                        1.00f to accent.copy(alpha = 0f)
                    )
                )
                val ambCx = w * 0.15f
                val ambCy = h * 0.88f
                val ambR = w * 0.62f
                nativePaint.shader = RadialGradient(
                    ambCx, ambCy, ambR,
                    ambCols, ambPos,
                    Shader.TileMode.CLAMP
                )
                nc.save()
                nc.scale(1.05f, 0.54f, ambCx, ambCy)
                nc.drawCircle(ambCx, ambCy, ambR, nativePaint)
                nc.restore()

                // 7) Smooth top cinematic vignette spanning full 0..1 of 0.30h
                val topVignetteH = h * 0.30f
                val (topVigCols, topVigPos) = buildCosineGradientStops(
                    listOf(
                        0.00f to Color.Black.copy(alpha = 0.68f),
                        0.45f to Color.Black.copy(alpha = 0.24f),
                        1.00f to Color.Black.copy(alpha = 0f)
                    ),
                    steps = 12
                )
                nativePaint.shader = LinearGradient(
                    0f, 0f, 0f, topVignetteH,
                    topVigCols, topVigPos,
                    Shader.TileMode.CLAMP
                )
                nc.drawRect(0f, 0f, w, topVignetteH, nativePaint)

                // 8) Bottom-right corner rich vignette (unclipped circle)
                val (brVigCols, brVigPos) = buildCosineGradientStops(
                    listOf(
                        0.00f to Color.Black.copy(alpha = 0.86f),
                        0.52f to Color.Black.copy(alpha = 0.36f),
                        1.00f to Color.Black.copy(alpha = 0f)
                    ),
                    steps = 12
                )
                val brCx = w * 1.05f
                val brCy = h * 1.05f
                val brR = w * 0.68f
                nativePaint.shader = RadialGradient(
                    brCx, brCy, brR,
                    brVigCols, brVigPos,
                    Shader.TileMode.CLAMP
                )
                nc.drawCircle(brCx, brCy, brR, nativePaint)

                // 9) Fast micro-dither pass for 8-bit TV panel smoothness
                nc.drawRect(0f, 0f, w, h, ditherPaint)
            }
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
            val hasValidCustomImage = (wallpaperMode == "custom" || wallpaperMode == "preset") &&
                customWallpaperUrl.isNotBlank() &&
                customWallpaperUrl != "aurora_gradient" &&
                !customWallpaperUrl.contains("1543946207")
            if (hasValidCustomImage) {
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
