package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    blurIntensity: Float = 0.6f,
    liquidGlassOpacity: Float = 0.7f,
    liquidGlassSpecular: Float = 0.8f,
    borderColor: Color = Color(0x40FFFFFF),
    glowingBorder: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveBlurPx = (50f * blurIntensity).coerceIn(2f, 100f)
    val effectiveBlurDp = (25f * blurIntensity).dp
    val activeBorderColor = if (glowingBorder) Color(0xFF66BB6A) else borderColor

    Box(modifier = modifier) {
        // Frosted Glass Layer with native blur support on Android 12+ (API 31+)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.graphicsLayer {
                            renderEffect = RenderEffect.createBlurEffect(
                                effectiveBlurPx, effectiveBlurPx, Shader.TileMode.MIRROR
                            ).asComposeRenderEffect()
                        }
                    } else {
                        Modifier.blur(effectiveBlurDp)
                    }
                )
                .background(
                    SolidColor(Color(0x20FFFFFF).copy(alpha = (0.32f * liquidGlassOpacity).coerceIn(0.08f, 0.95f)))
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            activeBorderColor.copy(alpha = (0.6f * liquidGlassSpecular).coerceIn(0.2f, 1.0f)),
                            Color.White.copy(alpha = (0.2f * liquidGlassSpecular).coerceIn(0.05f, 0.8f)),
                            activeBorderColor.copy(alpha = (0.4f * liquidGlassSpecular).coerceIn(0.1f, 1.0f))
                        )
                    ),
                    shape = shape
                )
        )

        // Specular Rim Light overlay for frosted glass reflection depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (0.15f * liquidGlassSpecular).coerceIn(0.02f, 0.8f)),
                            Color.Transparent
                        )
                    )
                )
        )

        content()
    }
}
