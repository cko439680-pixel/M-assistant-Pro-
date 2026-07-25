package com.example.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlin.random.Random

@Composable
fun WallpaperBackground(
    wallpaperSpec: String,
    accentColor: Color = Color(0xFF22C55E),
    blurIntensity: Float = 0.6f,
    uiBrightness: Float = 0.8f,
    modifier: Modifier = Modifier
) {
    val vignetteAlpha1 = ((1f - uiBrightness) * 0.7f).coerceIn(0.0f, 0.85f)
    val vignetteAlpha2 = ((1f - uiBrightness) * 0.9f + 0.35f).coerceIn(0.2f, 0.95f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0D14))
            .then(
                if (blurIntensity > 0.05f) {
                    Modifier.blur((18 * blurIntensity).dp)
                } else Modifier
            )
    ) {
        when {
            // Option C: Video Loop Wallpaper Engine
            wallpaperSpec.startsWith("video:") -> {
                val videoUriString = wallpaperSpec.removePrefix("video:")
                val context = LocalContext.current
                val videoUri = remember(videoUriString) {
                    if (videoUriString == "preset_cyber_loop") {
                        Uri.parse("android.resource://${context.packageName}/raw/cyber_loop")
                    } else {
                        try { Uri.parse(videoUriString) } catch (e: Exception) { null }
                    }
                }

                if (videoUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(videoUri)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    mp.setVolume(0f, 0f)
                                    start()
                                }
                                setOnErrorListener { _, _, _ -> true }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            if (!view.isPlaying) {
                                view.start()
                            }
                        }
                    )
                } else {
                    // Fallback to Live Animated Canvas
                    LiveMatrixCanvas(accentColor = accentColor)
                }
            }

            // Option A: Gallery Image Wallpaper
            wallpaperSpec.startsWith("gallery:") || wallpaperSpec.startsWith("content:") || wallpaperSpec.startsWith("file:") -> {
                val imageUri = wallpaperSpec.removePrefix("gallery:")
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Custom Wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Option B: Live Animated Canvas Wallpaper Engine
            wallpaperSpec.startsWith("live:") -> {
                when (wallpaperSpec.removePrefix("live:")) {
                    "cyber_matrix" -> LiveMatrixCanvas(accentColor = accentColor)
                    "neon_pulse" -> LiveNeonPulseCanvas(accentColor = accentColor)
                    "hyperspace" -> LiveHyperspaceCanvas(accentColor = accentColor)
                    else -> LiveAuroraCanvas(accentColor = accentColor)
                }
            }

            // Static Presets
            wallpaperSpec == "Dark Obsidian Matrix" -> LiveMatrixCanvas(accentColor = accentColor)
            wallpaperSpec == "Gold Gamer Aura" -> LiveAuroraCanvas(accentColor = Color(0xFFEAB308))
            wallpaperSpec == "Cosmic Starlight" -> LiveHyperspaceCanvas(accentColor = Color(0xFF3B82F6))
            else -> {
                // "Cyberpunk Neon" or default
                LiveNeonPulseCanvas(accentColor = accentColor)
            }
        }

        // Glassy Dark Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun LiveMatrixCanvas(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "matrix")
    val animTime by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrixFloat"
    )

    val drops = remember {
        List(25) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 0.8f + 0.4f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drops.forEach { (xPercent, initialYPercent, speed) ->
            val x = xPercent * w
            val currentY = ((initialYPercent * h) + (animTime * speed * 20f)) % h

            for (i in 0..8) {
                val dropY = currentY - (i * 24f)
                if (dropY in 0f..h) {
                    val alpha = (1f - (i / 9f)).coerceIn(0.1f, 1f)
                    drawCircle(
                        color = if (i == 0) Color.White else accentColor.copy(alpha = alpha * 0.7f),
                        radius = if (i == 0) 3.5f else 2f,
                        center = Offset(x, dropY)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveNeonPulseCanvas(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "neon")
    val pulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = pulse * 0.35f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = w * 0.7f
            )
        )

        val gridStep = 60f
        var y = 0f
        while (y < h) {
            drawLine(
                color = accentColor.copy(alpha = 0.06f * pulse),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += gridStep
        }
    }
}

@Composable
private fun LiveAuroraCanvas(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraShift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val rad = Math.toRadians(shift.toDouble())
        val offsetX = (Math.sin(rad) * w * 0.2f).toFloat()
        val offsetY = (Math.cos(rad) * h * 0.15f).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.45f),
                    Color(0xFF8B5CF6).copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f + offsetX, h * 0.35f + offsetY),
                radius = w * 0.8f
            )
        )
    }
}

@Composable
private fun LiveHyperspaceCanvas(accentColor: Color) {
    val transition = rememberInfiniteTransition(label = "hyperspace")
    val speed by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hyperspaceSpeed"
    )

    val stars = remember {
        List(40) {
            Pair(Random.nextFloat() * 360f, Random.nextFloat() * 0.8f + 0.2f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)

        stars.forEach { (angleDeg, distanceFactor) ->
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val progress = (distanceFactor + speed) % 1f
            val dist = progress * (size.width * 0.6f)

            val x = (center.x + dist * Math.cos(angleRad)).toFloat()
            val y = (center.y + dist * Math.sin(angleRad)).toFloat()

            val prevDist = (dist - 20f).coerceAtLeast(0f)
            val prevX = (center.x + prevDist * Math.cos(angleRad)).toFloat()
            val prevY = (center.y + prevDist * Math.sin(angleRad)).toFloat()

            drawLine(
                color = accentColor.copy(alpha = progress),
                start = Offset(prevX, prevY),
                end = Offset(x, y),
                strokeWidth = progress * 3f
            )
        }
    }
}
